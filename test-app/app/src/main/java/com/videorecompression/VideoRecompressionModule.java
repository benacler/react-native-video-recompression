package com.videorecompression;

import android.util.Log;
import android.media.MediaMetadataRetriever;
import com.facebook.react.bridge.*;
import java.io.File;

public class VideoRecompressionModule extends ReactContextBaseJavaModule {
    private static final String TAG = "VideoRecompression";

    public VideoRecompressionModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "VideoRecompression";
    }

    @ReactMethod
    public void processVideo(String inputPath, String outputPath, ReadableMap settings, Promise promise) {
        try {
            Log.d(TAG, "🔧 Processing video with settings:");
            Log.d(TAG, "   Input: " + inputPath);
            Log.d(TAG, "   Output: " + outputPath);
            
            // Log all the settings
            if (settings.hasKey("audioBitrate")) {
                Log.d(TAG, "   audioBitrate: " + settings.getInt("audioBitrate"));
            }
            if (settings.hasKey("audioCodec")) {
                Log.d(TAG, "   audioCodec: " + settings.getString("audioCodec"));
            }
            if (settings.hasKey("maxHeight")) {
                Log.d(TAG, "   maxHeight: " + settings.getInt("maxHeight"));
            }
            if (settings.hasKey("maxWidth")) {
                Log.d(TAG, "   maxWidth: " + settings.getInt("maxWidth"));
            }
            if (settings.hasKey("optimizeForNetwork")) {
                Log.d(TAG, "   optimizeForNetwork: " + settings.getBoolean("optimizeForNetwork"));
            }
            if (settings.hasKey("quality")) {
                Log.d(TAG, "   quality: " + settings.getDouble("quality"));
            }
            if (settings.hasKey("videoBitrate")) {
                Log.d(TAG, "   videoBitrate: " + settings.getInt("videoBitrate"));
            }
            if (settings.hasKey("videoCodec")) {
                Log.d(TAG, "   videoCodec: " + settings.getString("videoCodec"));
            }
            
            File inputFile = new File(inputPath);
            File outputFile = new File(outputPath);
            
            if (!inputFile.exists()) {
                promise.reject("FILE_NOT_FOUND", "Input file does not exist: " + inputPath);
                return;
            }
            
            long inputSize = inputFile.length();
            Log.d(TAG, "📊 Input file size: " + formatFileSize(inputSize));
            
            // Analyze input video
            try {
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(inputPath);
                
                String width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
                String height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
                String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                
                Log.d(TAG, "📹 Input video info:");
                Log.d(TAG, "   Resolution: " + width + "x" + height);
                Log.d(TAG, "   Duration: " + duration + "ms");
                
                retriever.release();
                
                // Determine processing action based on your settings
                int inputWidth = width != null ? Integer.parseInt(width) : 0;
                int inputHeight = height != null ? Integer.parseInt(height) : 0;
                int maxWidth = settings.hasKey("maxWidth") ? settings.getInt("maxWidth") : 1280;
                int maxHeight = settings.hasKey("maxHeight") ? settings.getInt("maxHeight") : 720;
                
                String action = "passthrough";
                if (inputWidth > maxWidth || inputHeight > maxHeight) {
                    action = "recompress";
                    Log.d(TAG, "🔄 Action: RECOMPRESS - Video exceeds max dimensions");
                } else if (inputSize > 5000000) { // 5MB threshold
                    action = "recompress"; 
                    Log.d(TAG, "🔄 Action: RECOMPRESS - File size too large");
                } else {
                    Log.d(TAG, "✅ Action: PASSTHROUGH - Video already optimized");
                }
                
                // For this test, simulate processing by creating a smaller output file
                // In the real implementation, this would use MediaMuxer/MediaCodec
                long outputSize;
                if ("recompress".equals(action)) {
                    // Simulate compression by creating a file that's 70% of original size
                    outputSize = (long)(inputSize * 0.7);
                    Log.d(TAG, "🎯 Simulating compression to 70% of original size");
                } else {
                    // Passthrough - same size
                    outputSize = inputSize;
                    Log.d(TAG, "📋 Passthrough - maintaining original size");
                }
                
                // Create output file with simulated size
                createTestOutputFile(outputFile, outputSize);
                
                WritableMap result = Arguments.createMap();
                result.putString("outputPath", outputPath);
                result.putString("action", action);
                result.putDouble("processingTime", 2000.0);
                
                // Add size info
                WritableMap originalInfo = Arguments.createMap();
                originalInfo.putInt("width", inputWidth);
                originalInfo.putInt("height", inputHeight);
                originalInfo.putDouble("fileSize", (double)inputSize);
                
                WritableMap finalInfo = Arguments.createMap();
                finalInfo.putInt("width", Math.min(inputWidth, maxWidth));
                finalInfo.putInt("height", Math.min(inputHeight, maxHeight));
                finalInfo.putDouble("fileSize", (double)outputSize);
                
                result.putString("originalInfo", originalInfo.toString());
                result.putString("finalInfo", finalInfo.toString());
                
                Log.d(TAG, "✅ Processing completed successfully");
                Log.d(TAG, "📊 Results:");
                Log.d(TAG, "   Action: " + action);
                Log.d(TAG, "   Input size: " + formatFileSize(inputSize));
                Log.d(TAG, "   Output size: " + formatFileSize(outputSize));
                
                promise.resolve(result);
                
            } catch (Exception e) {
                Log.e(TAG, "❌ Error analyzing video", e);
                promise.reject("ANALYSIS_ERROR", "Failed to analyze video: " + e.getMessage(), e);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Processing failed", e);
            promise.reject("PROCESSING_ERROR", "Failed to process video: " + e.getMessage(), e);
        }
    }
    
    private void createTestOutputFile(File outputFile, long targetSize) throws Exception {
        // Create a test output file with the target size
        byte[] buffer = new byte[8192];
        try (java.io.FileOutputStream fos = new java.io.FileOutputStream(outputFile)) {
            long written = 0;
            while (written < targetSize) {
                int toWrite = (int)Math.min(buffer.length, targetSize - written);
                fos.write(buffer, 0, toWrite);
                written += toWrite;
            }
        }
    }
    
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }
}
