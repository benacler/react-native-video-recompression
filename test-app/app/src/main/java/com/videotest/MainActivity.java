package com.videotest;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.ScrollView;
import android.util.Log;
import android.os.Handler;
import android.os.Looper;
import java.io.File;

// Import our video recompression library
import com.videorecompression.VideoRecompressionModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.Arguments;

public class MainActivity extends Activity {
    private static final String TAG = "VideoTest";
    private TextView logView;
    private StringBuilder logBuffer = new StringBuilder();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Create simple UI to show test results
        ScrollView scrollView = new ScrollView(this);
        logView = new TextView(this);
        logView.setText("Starting Video Recompression Test...\n");
        logView.setPadding(20, 20, 20, 20);
        scrollView.addView(logView);
        setContentView(scrollView);
        
        // Start the test in background
        new Thread(this::runVideoTest).start();
    }
    
    private void updateLog(String message) {
        Log.d(TAG, message);
        logBuffer.append(message).append("\n");
        
        // Update UI on main thread
        new Handler(Looper.getMainLooper()).post(() -> {
            logView.setText(logBuffer.toString());
        });
    }
    
    private void runVideoTest() {
        updateLog("🚀 Video Recompression Library Test");
        updateLog("===================================");
        
        try {
            // Create React context for the module
            ReactApplicationContext reactContext = new ReactApplicationContext(this);
            VideoRecompressionModule module = new VideoRecompressionModule(reactContext);
            
            updateLog("✅ VideoRecompressionModule created successfully");
            
            // Test file paths
            // Test the library with the exact settings the user wants
            // Use app's internal storage to avoid permission issues
            File appDir = new File(getFilesDir(), "videos");
            appDir.mkdirs();
            String inputPath = new File(appDir, "test-video.mp4").getAbsolutePath();
            String outputPath = new File(appDir, "compressed-output.mp4").getAbsolutePath();
            
            // Copy test video from tmp location to internal storage
            try {
                java.io.InputStream in = new java.io.FileInputStream("/data/local/tmp/test-video.mp4");
                java.io.OutputStream out = new java.io.FileOutputStream(inputPath);
                byte[] buffer = new byte[8192];
                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
                in.close();
                out.close();
                updateLog("✅ Video copied to app storage: " + inputPath);
            } catch (Exception e) {
                updateLog("❌ Failed to copy video: " + e.getMessage());
                return;
            }
            
            updateLog("📂 Input: " + inputPath);
            updateLog("📂 Output: " + outputPath);
            
            // Check input file
            File inputFile = new File(inputPath);
            if (!inputFile.exists()) {
                updateLog("❌ Input file not found!");
                updateLog("Please ensure test-quick.mp4 is in /sdcard/Download/");
                return;
            }
            
            long inputSize = inputFile.length();
            updateLog("✅ Input file found: " + formatFileSize(inputSize));
            
            // Delete existing output
            new File(outputPath).delete();
            
            // Create test settings (your specific configuration)
            WritableMap settings = Arguments.createMap();
            settings.putInt("audioBitrate", 128000);
            settings.putString("audioCodec", "aac");
            settings.putInt("maxHeight", 720);
            settings.putInt("maxWidth", 1280);
            settings.putBoolean("optimizeForNetwork", true);
            settings.putDouble("quality", 0.7);
            settings.putInt("videoBitrate", 800000);
            settings.putString("videoCodec", "h264");
            
            updateLog("⚙️ Test Settings:");
            updateLog("   audioBitrate: 128000");
            updateLog("   audioCodec: aac");
            updateLog("   maxHeight: 720");
            updateLog("   maxWidth: 1280");
            updateLog("   optimizeForNetwork: true");
            updateLog("   quality: 0.7");
            updateLog("   videoBitrate: 800000");
            updateLog("   videoCodec: h264");
            
            // Test the processing
            updateLog("🔧 Starting video processing...");
            TestPromise promise = new TestPromise();
            
            long startTime = System.currentTimeMillis();
            // Cast WritableMap to ReadableMap for the method call
            module.processVideo(inputPath, outputPath, (ReadableMap)settings, promise);
            
            // Wait for completion
            int timeout = 60000; // 60 seconds
            int elapsed = 0;
            while (!promise.isCompleted() && elapsed < timeout) {
                Thread.sleep(1000);
                elapsed += 1000;
                updateLog("⏳ Processing... " + elapsed/1000 + "s");
            }
            
            long endTime = System.currentTimeMillis();
            long processingTime = endTime - startTime;
            
            if (promise.isResolved()) {
                updateLog("✅ Processing completed successfully!");
                updateLog("⏱️ Processing time: " + processingTime + "ms");
                
                // Check output file
                File outputFile = new File(outputPath);
                if (outputFile.exists()) {
                    long outputSize = outputFile.length();
                    double reduction = ((double)(inputSize - outputSize) / inputSize) * 100;
                    
                    updateLog("📊 Results:");
                    updateLog("   Input size:  " + formatFileSize(inputSize));
                    updateLog("   Output size: " + formatFileSize(outputSize));
                    updateLog("   Reduction:   " + String.format("%.1f%%", reduction));
                    updateLog("   Time:        " + processingTime + "ms");
                    updateLog("");
                    updateLog("🎉 TEST PASSED!");
                } else {
                    updateLog("❌ Output file not created");
                    updateLog("Processing may have failed silently");
                }
            } else if (promise.isRejected()) {
                updateLog("❌ Processing failed:");
                updateLog("   Error: " + promise.getError());
                updateLog("");
                updateLog("This might indicate the state management issue still exists");
            } else {
                updateLog("❌ Processing timed out after " + timeout/1000 + " seconds");
            }
            
        } catch (Exception e) {
            updateLog("❌ Test failed with exception:");
            updateLog("   " + e.getClass().getSimpleName() + ": " + e.getMessage());
            if (e.getCause() != null) {
                updateLog("   Cause: " + e.getCause().getMessage());
            }
            Log.e(TAG, "Test failed", e);
        }
        
        updateLog("");
        updateLog("📋 Test completed. Check the logs above for results.");
    }
    
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }
    
    // Simple Promise implementation for testing
    private static class TestPromise implements Promise {
        private volatile boolean resolved = false;
        private volatile boolean rejected = false;
        private volatile String error = null;
        
        @Override
        public void resolve(Object value) {
            resolved = true;
            Log.d("VideoTest", "✅ Promise resolved: " + value);
        }
        
        @Override
        public void reject(String code, String message) {
            rejected = true;
            error = code + ": " + message;
            Log.e("VideoTest", "❌ Promise rejected: " + error);
        }
        
        @Override
        public void reject(String code, String message, Throwable throwable) {
            rejected = true;
            error = code + ": " + message;
            Log.e("VideoTest", "❌ Promise rejected: " + error, throwable);
        }
        
        @Override
        public void reject(String code, Throwable throwable) {
            rejected = true;
            error = code + ": " + throwable.getMessage();
            Log.e("VideoTest", "❌ Promise rejected: " + error, throwable);
        }
        
        @Override
        public void reject(Throwable throwable) {
            rejected = true;
            error = throwable.getMessage();
            Log.e("VideoTest", "❌ Promise rejected: " + error, throwable);
        }
        
        @Override
        public void reject(String message) {
            rejected = true;
            error = message;
            Log.e("VideoTest", "❌ Promise rejected: " + error);
        }
        
        public boolean isResolved() { return resolved; }
        public boolean isRejected() { return rejected; }
        public String getError() { return error; }
        public boolean isCompleted() { return resolved || rejected; }
    }
}
