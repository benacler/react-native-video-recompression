package com.videorecompression;

import android.util.Log;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;
import java.io.File;

public class VideoRecompressionTest {
    private static final String TAG = "VideoRecompressionTest";
    
    public static void runQuickTest(ReactApplicationContext context) {
        Log.d(TAG, "Starting quick video compression test...");
        
        try {
            VideoRecompressionModule module = new VideoRecompressionModule(context);
            
            // Test paths
            String inputPath = "/sdcard/Download/test-quick.mp4";
            String outputPath = "/sdcard/Download/compressed-output.mp4";
            
            Log.d(TAG, "Input: " + inputPath);
            Log.d(TAG, "Output: " + outputPath);
            
            // Check if input file exists
            File inputFile = new File(inputPath);
            if (!inputFile.exists()) {
                Log.e(TAG, "❌ Input file not found: " + inputPath);
                return;
            }
            
            Log.d(TAG, "✅ Input file found, size: " + inputFile.length() + " bytes");
            
            // Create test settings
            WritableMap settings = Arguments.createMap();
            settings.putInt("audioBitrate", 128000);
            settings.putString("audioCodec", "aac");
            settings.putInt("maxHeight", 720);
            settings.putInt("maxWidth", 1280);
            settings.putBoolean("optimizeForNetwork", true);
            settings.putDouble("quality", 0.7);
            settings.putInt("videoBitrate", 800000);
            settings.putString("videoCodec", "h264");
            
            Log.d(TAG, "🔧 Starting video processing with compression settings...");
            
            // Test the compression
            TestPromise promise = new TestPromise();
            module.processVideo(inputPath, outputPath, settings, promise);
            
            // Wait for completion
            int waitTime = 0;
            while (!promise.isCompleted() && waitTime < 30000) {
                Thread.sleep(500);
                waitTime += 500;
            }
            
            if (promise.isResolved()) {
                Log.d(TAG, "✅ Video compression completed successfully!");
                File outputFile = new File(outputPath);
                if (outputFile.exists()) {
                    long inputSize = inputFile.length();
                    long outputSize = outputFile.length();
                    double reduction = ((double)(inputSize - outputSize) / inputSize) * 100;
                    
                    Log.d(TAG, "📊 Results:");
                    Log.d(TAG, "   Input size:  " + inputSize + " bytes");
                    Log.d(TAG, "   Output size: " + outputSize + " bytes");
                    Log.d(TAG, "   Reduction:   " + String.format("%.1f", reduction) + "%");
                    Log.d(TAG, "🎉 TEST PASSED - Video compression successful!");
                } else {
                    Log.e(TAG, "❌ Output file not created");
                }
            } else if (promise.isRejected()) {
                Log.e(TAG, "❌ Processing failed: " + promise.getError());
            } else {
                Log.e(TAG, "❌ Processing timed out after 30 seconds");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Test failed with exception", e);
        }
    }
    
    // Simplified Promise implementation for testing
    private static class TestPromise implements Promise {
        private boolean resolved = false;
        private boolean rejected = false;
        private String error = null;
        
        @Override
        public void resolve(Object value) {
            resolved = true;
            Log.d("VideoRecompressionTest", "✅ Promise resolved: " + value);
        }
        
        @Override
        public void reject(String code, String message) {
            rejected = true;
            error = code + ": " + message;
            Log.e("VideoRecompressionTest", "❌ Promise rejected: " + error);
        }
        
        @Override
        public void reject(String code, String message, Throwable throwable) {
            rejected = true;
            error = code + ": " + message;
            Log.e("VideoRecompressionTest", "❌ Promise rejected: " + error, throwable);
        }
        
        @Override
        public void reject(String code, Throwable throwable) {
            rejected = true;
            error = code + ": " + throwable.getMessage();
            Log.e("VideoRecompressionTest", "❌ Promise rejected: " + error, throwable);
        }
        
        @Override
        public void reject(Throwable throwable) {
            rejected = true;
            error = throwable.getMessage();
            Log.e("VideoRecompressionTest", "❌ Promise rejected: " + error, throwable);
        }
        
        @Override
        public void reject(String message) {
            rejected = true;
            error = message;
            Log.e("VideoRecompressionTest", "❌ Promise rejected: " + error);
        }
        
        public boolean isResolved() { return resolved; }
        public boolean isRejected() { return rejected; }
        public String getError() { return error; }
        public boolean isCompleted() { return resolved || rejected; }
    }
}
