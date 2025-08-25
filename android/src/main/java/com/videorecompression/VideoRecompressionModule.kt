package com.videorecompression

import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule
import kotlinx.coroutines.*
import android.media.MediaMetadataRetriever
import android.media.MediaFormat
import android.media.MediaMuxer
import android.media.MediaExtractor
import android.net.Uri
import java.io.File
import java.io.IOException

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
                putString("version", "1.0.0")
                putArray("capabilities", WritableNativeArray().apply {
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
                
                // For now, implement a simple copy/move operation
                // In a full implementation, you would add actual video processing logic here
                val inputFile = File(inputPath)
                val outputFile = File(outputPath)
                
                // Ensure output directory exists
                outputFile.parentFile?.mkdirs()
                
                // Simple file copy for MVP
                inputFile.copyTo(outputFile, overwrite = true)
                
                val finalInfo = getVideoInfo(outputPath)
                val processingTime = System.currentTimeMillis() - startTime
                
                val result = WritableNativeMap().apply {
                    putString("outputPath", outputPath)
                    putString("action", "passthrough")
                    putMap("originalInfo", originalInfo)
                    putMap("finalInfo", finalInfo)
                    putDouble("processingTime", processingTime.toDouble())
                }
                
                promise.resolve(result)
            } catch (e: Exception) {
                promise.reject("PROCESS_ERROR", "Failed to process video: ${e.message}", e)
            }
        }
    }

    private fun getVideoInfo(filePath: String): WritableMap {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(filePath)
            
            WritableNativeMap().apply {
                putString("container", getFileExtension(filePath))
                putString("videoCodec", getVideoCodecFromFile(filePath))
                putString("audioCodec", "aac") // Default assumption
                putInt("width", retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0)
                putInt("height", retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0)
                putDouble("duration", (retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L) / 1000.0)
                putInt("videoBitrate", retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toIntOrNull() ?: 0)
                putInt("audioBitrate", 128000) // Default assumption
                putDouble("frameRate", (retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE)?.toDoubleOrNull() ?: 30.0))
                putDouble("fileSize", File(filePath).length().toDouble())
            }
        } catch (e: Exception) {
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
            } catch (e: Exception) {
                // Ignore release errors
            }
        }
    }

    private fun getFileExtension(filePath: String): String {
        return filePath.substringAfterLast('.', "unknown").lowercase()
    }

    private fun getVideoCodecFromFile(filePath: String): String {
        val extension = getFileExtension(filePath)
        return when (extension) {
            "mp4", "m4v" -> "h264"
            "mp4" -> "h264" // MP4 files typically use H.264
            "avi" -> "h264" // AVI files often use H.264
            "avi" -> "unknown" // AVI can contain various codecs
            "mkv" -> "unknown" // MKV can contain various codecs
            "webm" -> "vp8"
            else -> "unknown"
        }
    }

    override fun onCatalystInstanceDestroy() {
        super.onCatalystInstanceDestroy()
        scope.cancel()
    }
}
