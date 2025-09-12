package com.videorecompression

import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule
import kotlinx.coroutines.*
import android.media.MediaMetadataRetriever
import android.media.MediaFormat
import android.media.MediaMuxer
import android.media.MediaExtractor
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer

data class TrackInfo(
    val videoCodec: String,
    val audioCodec: String,
    val videoBitrate: Int,
    val audioBitrate: Int
)

class VideoRecompressionModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun getName(): String {
        return "VideoRecompression"
    }

    @ReactMethod
    fun init(promise: Promise) {
        try {
            val result = WritableNativeMap().apply {
                putString("platform", "android")
                putString("version", "0.9.2")
                putArray("capabilities", WritableNativeArray().apply {
                    pushString("video_analysis")
                    pushString("smart_compression")
                    pushString("codec_detection")
                    pushString("container_rewrap")
                    pushString("h264")
                    pushString("aac")
                    pushString("mp4")
                    pushString("avi")
                    pushString("webm")
                })
            }
            promise.resolve(result)
        } catch (e: Exception) {
            promise.reject("INIT_ERROR", "Failed to initialize video recompression module", e)
        }
    }

    @ReactMethod
    fun analyzeVideo(filePath: String, promise: Promise) {
        scope.launch {
            try {
                val videoInfo = getVideoInfo(filePath)
                promise.resolve(videoInfo)
            } catch (e: Exception) {
                promise.reject("ANALYZE_ERROR", "Failed to analyze video: ${e.message}", e)
            }
        }
    }

    @ReactMethod
    fun processVideo(
        inputPath: String,
        outputPath: String,
        settings: ReadableMap?,
        promise: Promise
    ) {
        scope.launch {
            try {
                val startTime = System.currentTimeMillis()
                val originalInfo = getVideoInfo(inputPath)
                
                // Ensure output directory exists
                File(outputPath).parentFile?.mkdirs()
                
                // Analyze input and determine processing strategy
                val inputContainer = getFileExtension(inputPath)
                val currentVideoCodec = originalInfo.getString("videoCodec") ?: "unknown"
                val currentAudioCodec = originalInfo.getString("audioCodec") ?: "unknown"
                
                // Get target settings with defaults
                val targetVideoCodec = settings?.getString("videoCodec") ?: "h264"
                val targetAudioCodec = settings?.getString("audioCodec") ?: "aac"
                val targetContainer = "mp4"
                val quality = settings?.getDouble("quality") ?: 0.8
                
                // Decision logic for processing strategy
                val action = determineProcessingAction(
                    inputContainer, currentVideoCodec, currentAudioCodec,
                    targetContainer, targetVideoCodec, targetAudioCodec,
                    originalInfo.getInt("videoBitrate"),
                    originalInfo.getInt("audioBitrate"),
                    settings?.getInt("videoBitrate") ?: 800000,
                    settings?.getInt("audioBitrate") ?: 128000
                )
                
                Log.d("VideoRecompression", "Processing action: $action")
                Log.d("VideoRecompression", "Input: $inputContainer/$currentVideoCodec/$currentAudioCodec")
                Log.d("VideoRecompression", "Target: $targetContainer/$targetVideoCodec/$targetAudioCodec")
                
                when (action) {
                    "passthrough" -> {
                        // Just copy the file - already in optimal format
                        File(inputPath).copyTo(File(outputPath), overwrite = true)
                    }
                    "rewrap" -> {
                        // Change container but keep codecs - use MediaMuxer
                        rewrapVideo(inputPath, outputPath)
                    }
                    "recompress" -> {
                        // Full transcoding needed
                        transcodeVideo(inputPath, outputPath, settings)
                    }
                }
                
                val finalInfo = getVideoInfo(outputPath)
                val processingTime = System.currentTimeMillis() - startTime
                
                val result = WritableNativeMap().apply {
                    putString("outputPath", outputPath)
                    putString("action", action)
                    putMap("originalInfo", originalInfo)
                    putMap("finalInfo", finalInfo)
                    putDouble("processingTime", processingTime.toDouble())
                }
                
                promise.resolve(result)
            } catch (e: Exception) {
                Log.e("VideoRecompression", "Failed to process video", e)
                promise.reject("PROCESS_ERROR", "Failed to process video: ${e.message}", e)
            }
        }
    }

    private fun getVideoInfo(filePath: String): WritableMap {
        val retriever = MediaMetadataRetriever()
        val extractor = MediaExtractor()
        
        return try {
            retriever.setDataSource(filePath)
            extractor.setDataSource(filePath)
            
            // Get basic video properties from MediaMetadataRetriever
            val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0
            val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0
            val duration = (retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L) / 1000.0
            val frameRate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE)?.toDoubleOrNull() ?: 30.0
            val fileSize = File(filePath).length().toDouble()
            
            // Analyze tracks using MediaExtractor for accurate codec and bitrate detection
            val trackInfo = analyzeTracksWithExtractor(extractor, duration)
            
            WritableNativeMap().apply {
                putString("container", getFileExtension(filePath))
                putString("videoCodec", trackInfo.videoCodec)
                putString("audioCodec", trackInfo.audioCodec)
                putInt("width", width)
                putInt("height", height)
                putDouble("duration", duration)
                putInt("videoBitrate", trackInfo.videoBitrate)
                putInt("audioBitrate", trackInfo.audioBitrate)
                putDouble("frameRate", frameRate)
                putDouble("fileSize", fileSize)
            }
        } catch (e: Exception) {
            Log.e("VideoRecompression", "Error analyzing video: ${e.message}", e)
            WritableNativeMap().apply {
                putString("container", getFileExtension(filePath))
                putString("videoCodec", "unknown")
                putString("audioCodec", "unknown")
                putInt("width", 0)
                putInt("height", 0)
                putDouble("duration", 0.0)
                putInt("videoBitrate", 0)
                putInt("audioBitrate", 0)
                putDouble("frameRate", 0.0)
                putDouble("fileSize", File(filePath).length().toDouble())
            }
        } finally {
            try {
                retriever.release()
                extractor.release()
            } catch (e: Exception) {
                // Ignore release errors
            }
        }
    }

    private fun analyzeTracksWithExtractor(extractor: MediaExtractor, durationSeconds: Double): TrackInfo {
        var videoCodec = "unknown"
        var audioCodec = "unknown"
        var videoBitrate = 0
        var audioBitrate = 0
        
        try {
            val trackCount = extractor.trackCount
            
            for (i in 0 until trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
                
                when {
                    mime.startsWith("video/") -> {
                        // Detect video codec from MIME type
                        videoCodec = when {
                            mime.contains("avc") || mime.contains("h264") -> "h264"
                            mime.contains("hevc") || mime.contains("h265") -> "hevc"
                            mime.contains("vp8") -> "vp8"
                            mime.contains("vp9") -> "vp9"
                            mime.contains("av01") -> "av1"
                            else -> "unknown"
                        }
                        
                        // Get video bitrate from format
                        videoBitrate = if (format.containsKey(MediaFormat.KEY_BIT_RATE)) {
                            format.getInteger(MediaFormat.KEY_BIT_RATE)
                        } else {
                            // Fallback: estimate from file size if duration available
                            if (durationSeconds > 0) {
                                // Rough estimate: assume 80% of file is video
                                val fileSize = try { 
                                    // This is approximate since we don't have direct access to file here
                                    // In practice, this would be calculated differently
                                    0 
                                } catch (e: Exception) { 0 }
                                if (fileSize > 0) (fileSize * 8 * 0.8 / durationSeconds).toInt() else 0
                            } else 0
                        }
                    }
                    
                    mime.startsWith("audio/") -> {
                        // Detect audio codec from MIME type
                        audioCodec = when {
                            mime.contains("mp4a") || mime.contains("aac") -> "aac"
                            mime.contains("mp3") -> "mp3"
                            mime.contains("opus") -> "opus"
                            mime.contains("vorbis") -> "vorbis"
                            mime.contains("flac") -> "flac"
                            else -> "unknown"
                        }
                        
                        // Get audio bitrate from format
                        audioBitrate = if (format.containsKey(MediaFormat.KEY_BIT_RATE)) {
                            format.getInteger(MediaFormat.KEY_BIT_RATE)
                        } else {
                            // Default assumption for common audio formats
                            when (audioCodec) {
                                "aac" -> 128000  // 128 kbps is common for AAC
                                "mp3" -> 128000  // 128 kbps is common for MP3
                                "opus" -> 96000  // 96 kbps is common for Opus
                                else -> 128000   // Default fallback
                            }
                        }
                    }
                }
            }
            
            // If no audio track found
            if (audioCodec == "unknown") {
                audioCodec = "none"
                audioBitrate = 0
            }
            
        } catch (e: Exception) {
            Log.w("VideoRecompression", "Error analyzing tracks with extractor: ${e.message}")
            // Return fallback values
            videoCodec = "unknown"
            audioCodec = "unknown" 
            videoBitrate = 0
            audioBitrate = 0
        }
        
        return TrackInfo(videoCodec, audioCodec, videoBitrate, audioBitrate)
    }

    private fun determineProcessingAction(
        inputContainer: String, currentVideoCodec: String, currentAudioCodec: String,
        targetContainer: String, targetVideoCodec: String, targetAudioCodec: String,
        currentVideoBitrate: Int = 0, currentAudioBitrate: Int = 0,
        targetVideoBitrate: Int = 800000, targetAudioBitrate: Int = 128000
    ): String {
        
        // Define thresholds for chat optimization
        val videoRecompressionThreshold = 2000000 // 2 Mbps - recompress if higher
        val audioRecompressionThreshold = 192000  // 192 kbps - recompress if higher
        
        // 1. Check if codecs match target
        val codecsMatch = currentVideoCodec == targetVideoCodec && currentAudioCodec == targetAudioCodec
        
        // 2. Check if container matches target
        val containerMatches = inputContainer == targetContainer
        
        // 3. Check if bitrates are reasonable for chat
        val videoBitrateReasonable = currentVideoBitrate <= videoRecompressionThreshold
        val audioBitrateReasonable = currentAudioBitrate <= audioRecompressionThreshold
        
        Log.d("VideoRecompression", "Decision factors:")
        Log.d("VideoRecompression", "  Codecs match: $codecsMatch ($currentVideoCodec==$targetVideoCodec, $currentAudioCodec==$targetAudioCodec)")
        Log.d("VideoRecompression", "  Container matches: $containerMatches ($inputContainer==$targetContainer)")
        Log.d("VideoRecompression", "  Video bitrate reasonable: $videoBitrateReasonable ($currentVideoBitrate <= $videoRecompressionThreshold)")
        Log.d("VideoRecompression", "  Audio bitrate reasonable: $audioBitrateReasonable ($currentAudioBitrate <= $audioRecompressionThreshold)")
        
        return when {
            // Perfect case: everything matches and bitrates are reasonable
            codecsMatch && containerMatches && videoBitrateReasonable && audioBitrateReasonable -> {
                Log.d("VideoRecompression", "Decision: PASSTHROUGH - Already optimal")
                "passthrough"
            }
            
            // Good codecs but wrong container or slightly high bitrate but within rewrap threshold
            codecsMatch && videoBitrateReasonable && audioBitrateReasonable -> {
                Log.d("VideoRecompression", "Decision: REWRAP - Correct codecs, change container or minor optimization")
                "rewrap"
            }
            
            // High bitrates or wrong codecs - need full recompression
            else -> {
                Log.d("VideoRecompression", "Decision: RECOMPRESS - Bitrates too high or wrong codecs")
                "recompress"
            }
        }
    }
    
    private fun rewrapVideo(inputPath: String, outputPath: String) {
        val extractor = MediaExtractor()
        var muxer: MediaMuxer? = null
        
        try {
            extractor.setDataSource(inputPath)
            muxer = MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            
            val trackCount = extractor.trackCount
            val trackIndexMap = mutableMapOf<Int, Int>()
            
            // Add all tracks to muxer
            for (i in 0 until trackCount) {
                val format = extractor.getTrackFormat(i)
                val muxerTrackIndex = muxer.addTrack(format)
                trackIndexMap[i] = muxerTrackIndex
            }
            
            muxer.start()
            
            // Copy data from all tracks
            for (i in 0 until trackCount) {
                extractor.selectTrack(i)
                copyTrack(extractor, muxer, trackIndexMap[i]!!)
                extractor.unselectTrack(i)
            }
            
        } finally {
            extractor.release()
            muxer?.stop()
            muxer?.release()
        }
    }
    
    private fun copyTrack(extractor: MediaExtractor, muxer: MediaMuxer, muxerTrackIndex: Int) {
        val bufferInfo = MediaCodec.BufferInfo()
        val buffer = ByteBuffer.allocate(1024 * 1024) // 1MB buffer
        
        while (true) {
            val sampleSize = extractor.readSampleData(buffer, 0)
            if (sampleSize < 0) break
            
            bufferInfo.presentationTimeUs = extractor.sampleTime
            bufferInfo.flags = extractor.sampleFlags
            bufferInfo.offset = 0
            bufferInfo.size = sampleSize
            
            muxer.writeSampleData(muxerTrackIndex, buffer, bufferInfo)
            extractor.advance()
        }
    }
    
    private fun transcodeVideo(inputPath: String, outputPath: String, settings: ReadableMap?) {
        // For now, implement a basic transcoding using MediaMuxer and MediaCodec
        // This is a simplified version - a full implementation would need more sophisticated codec handling
        
        val extractor = MediaExtractor()
        var muxer: MediaMuxer? = null
        
        try {
            extractor.setDataSource(inputPath)
            muxer = MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            
            // Find video and audio tracks
            var videoTrackIndex = -1
            var audioTrackIndex = -1
            
            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
                
                when {
                    mime.startsWith("video/") -> videoTrackIndex = i
                    mime.startsWith("audio/") -> audioTrackIndex = i
                }
            }
            
            // For simplified transcoding, we'll use MediaMuxer to copy tracks
            // In a full implementation, you'd use MediaCodec for actual transcoding
            val trackIndexMap = mutableMapOf<Int, Int>()
            
            if (videoTrackIndex >= 0) {
                val videoFormat = extractor.getTrackFormat(videoTrackIndex)
                // Apply video settings if provided
                settings?.let { applyVideoSettings(videoFormat, it) }
                val muxerVideoIndex = muxer.addTrack(videoFormat)
                trackIndexMap[videoTrackIndex] = muxerVideoIndex
            }
            
            if (audioTrackIndex >= 0) {
                val audioFormat = extractor.getTrackFormat(audioTrackIndex)
                // Apply audio settings if provided
                settings?.let { applyAudioSettings(audioFormat, it) }
                val muxerAudioIndex = muxer.addTrack(audioFormat)
                trackIndexMap[audioTrackIndex] = muxerAudioIndex
            }
            
            muxer.start()
            
            // Copy tracks (simplified - doesn't actually transcode)
            trackIndexMap.forEach { (extractorIndex, muxerIndex) ->
                extractor.selectTrack(extractorIndex)
                copyTrack(extractor, muxer, muxerIndex)
                extractor.unselectTrack(extractorIndex)
            }
            
        } finally {
            extractor.release()
            muxer?.stop()
            muxer?.release()
        }
    }
    
    private fun applyVideoSettings(format: MediaFormat, settings: ReadableMap) {
        // Apply video compression settings
        settings.getInt("maxWidth").takeIf { it > 0 }?.let { 
            format.setInteger(MediaFormat.KEY_WIDTH, it) 
        }
        settings.getInt("maxHeight").takeIf { it > 0 }?.let { 
            format.setInteger(MediaFormat.KEY_HEIGHT, it) 
        }
        settings.getInt("videoBitrate").takeIf { it > 0 }?.let { 
            format.setInteger(MediaFormat.KEY_BIT_RATE, it) 
        }
        settings.getInt("frameRate").takeIf { it > 0 }?.let { 
            format.setInteger(MediaFormat.KEY_FRAME_RATE, it) 
        }
    }
    
    private fun applyAudioSettings(format: MediaFormat, settings: ReadableMap) {
        // Apply audio compression settings
        settings.getInt("audioBitrate").takeIf { it > 0 }?.let { 
            format.setInteger(MediaFormat.KEY_BIT_RATE, it) 
        }
    }
    
    private fun getFileExtension(filePath: String): String {
        return filePath.substringAfterLast('.', "unknown").lowercase()
    }

    override fun onCatalystInstanceDestroy() {
        super.onCatalystInstanceDestroy()
        scope.cancel()
    }
}
