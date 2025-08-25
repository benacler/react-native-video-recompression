# react-native-video-recompression

[![npm version](https://badge.fury.io/js/react-native-video-recompression.svg)](https://badge.fury.io/js/react-native-video-recompression)
[![npm downloads](https://img.shields.io/npm/dm/react-native-video-recompression.svg)](https://npmjs.org/package/react-native-video-recompression)
[![CI](https://github.com/YOUR_USERNAME/react-native-video-recompression/workflows/CI/badge.svg)](https://github.com/YOUR_USERNAME/react-native-video-recompression/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

A React Native library for intelligent video processing with native performance. Supports video analysis, quality-preserving format conversion, and smart recompression with customizable settings.

## 🚀 Quick Start

### Installation

```bash
npm install react-native-video-recompression
# or
yarn add react-native-video-recompression
```

### iOS Setup

```bash
cd ios && pod install
```

### Android Setup

No additional setup required - auto-linking handles everything!

## 📱 Testing with Example App

We provide a complete example app to test all functionality:

```bash
# 1. Generate the React Native example app
./scripts/setup-example.sh

# 2. Navigate to example directory  
cd example

# 3. Run on iOS
npm run ios

# 4. Run on Android
npm run android
```

The example app includes:
- Interactive testing interface
- All API methods demonstrated
- Progress tracking
- Error handling examples
- Performance metrics

## Usage

```javascript
import VideoRecompression from 'react-native-video-recompression';

// Initialize and test the module
const initializeModule = async () => {
  try {
    const info = await VideoRecompression.init();
    console.log('Module initialized:', info);
    // Output: { platform: 'ios', version: '2.0.0', capabilities: ['video_analysis', 'smart_compression', ...] }
  } catch (error) {
    console.error('Module initialization failed:', error);
  }
};

// Analyze video file to get detailed information
const analyzeVideo = async (videoPath) => {
  try {
    const videoInfo = await VideoRecompression.analyzeVideo(videoPath);
    console.log('Video analysis:', videoInfo);
    // Output: { container: 'mp4', videoCodec: 'h264', width: 1920, height: 1080, duration: 120, ... }
  } catch (error) {
    console.error('Video analysis failed:', error);
  }
};

// Smart video processing with optimal strategy selection
const processVideo = async (inputPath, outputPath) => {
  try {
    const result = await VideoRecompression.processVideo(
      inputPath,
      outputPath,
      {
        // Optional: Custom compression settings
        videoCodec: 'h264',
        quality: 0.8,
        maxWidth: 1920,
        optimizeForNetwork: true
      },
      (progress) => {
        console.log(`Processing: ${Math.round(progress * 100)}%`);
      }
    );
    
    console.log('Processing result:', result);
    // Output: { 
    //   outputPath: '/path/to/output.mp4',
    //   action: 'passthrough', // or 'rewrap' or 'recompress'
    //   originalInfo: {...},
    //   finalInfo: {...},
    //   processingTime: 1500
    // }
  } catch (error) {
    console.error('Video processing failed:', error);
  }
};
```

## API

### `init(): Promise<object>`

Initializes the module and returns platform information.

**Returns:**
- Promise<object>: Resolves to module information including:
  - `platform`: 'ios' | 'android'
  - `version`: Module version
  - `capabilities`: Array of supported features

### `analyzeVideo(filePath: string): Promise<VideoInfo>`

Analyzes a video file and returns comprehensive information about its properties.

**Parameters:**
- `filePath` (string): Absolute path to the video file

**Returns:**
- Promise<VideoInfo>: Detailed video information including:
  - `container`: File format (mp4, mov, avi, etc.)
  - `videoCodec`: Video codec (h264, hevc, vp9, etc.)
  - `audioCodec`: Audio codec (aac, mp3, pcm, etc.)
  - `width`, `height`: Video dimensions
  - `duration`: Duration in seconds
  - `videoBitrate`, `audioBitrate`: Bitrates in bits per second
  - `frameRate`: Frames per second
  - `fileSize`: File size in bytes

### `processVideo(inputPath, outputPath, settings?, onProgress?): Promise<CompressionResult>`

Intelligently processes video with optimal strategy selection (passthrough, rewrap, or recompress).

**Parameters:**
- `inputPath` (string): Absolute path to input video
- `outputPath` (string): Absolute path for output video
- `settings` (optional): Compression settings object
- `onProgress` (optional): Progress callback function (0.0 to 1.0)

**Returns:**
- Promise<CompressionResult>: Processing result including:
  - `outputPath`: Path to processed video
  - `action`: Strategy used ('passthrough' | 'rewrap' | 'recompress')
  - `originalInfo`: Input video information
  - `finalInfo`: Output video information
  - `processingTime`: Processing time in milliseconds

**Processing Strategies:**
- **Passthrough**: File is already optimal, no changes made
- **Rewrap**: Changes container format while preserving video/audio quality
- **Recompress**: Applies compression settings to reduce file size or change quality

## Features

- ✅ **Smart Processing**: Automatically chooses optimal strategy (passthrough/rewrap/recompress)
- ✅ **Video Analysis**: Get detailed information about video files (codec, bitrate, dimensions, etc.)
- ✅ **Quality Preservation**: Lossless format conversion when recompression isn't needed
- ✅ **Custom Compression**: Fine-tune quality, bitrate, resolution, and codec settings
- ✅ **Progress Callbacks**: Real-time progress updates for long-running operations
- ✅ **Cross Platform**: Native implementations for both iOS and Android
- ✅ **Background Processing**: Non-blocking operations using background threads
- ✅ **Zero Configuration**: Works out of the box with React Native autolinking
- ✅ **TypeScript Support**: Full TypeScript definitions included
- ✅ **Multiple Formats**: Support for MP4, AVI, WEBM, and other common video formats

## Use Cases

- **Format Conversion**: Convert between video formats while preserving quality
- **File Size Optimization**: Reduce video file sizes for storage or network transmission
- **Video Analysis**: Extract metadata and technical information from video files
- **Quality Adjustment**: Change video quality, resolution, or codec for specific requirements
- **Batch Processing**: Process multiple videos with consistent settings

## Technical Details

### iOS Implementation
- Uses `AVFoundation` framework with `AVAssetExportSession`
- **Smart Strategy Selection**:
  - **Passthrough**: File already in optimal format
  - **Rewrap**: Uses `AVAssetExportPresetPassthrough` to change container without reencoding
  - **Recompress**: Uses quality presets or custom export settings for size reduction
- Video analysis via `AVURLAsset` and format descriptions
- Background processing on dedicated queues to avoid UI blocking
- Native progress callbacks and error handling

### Android Implementation  
- Uses `MediaMuxer`, `MediaExtractor`, and `MediaMetadataRetriever`
- **Smart Strategy Selection**:
  - **Passthrough**: File meets target requirements
  - **Rewrap**: Container format change using MediaMuxer without reencoding
  - **Recompress**: Full transcoding with MediaCodec when quality adjustment needed
- Comprehensive video analysis including codec detection and bitrate estimation
- Kotlin coroutines for asynchronous background processing
- Support for both video and audio track processing

### Performance Optimizations
- Lazy loading of native modules
- Memory-efficient streaming for large video files
- Automatic cleanup of temporary resources
- Optimized codec selection based on device capabilities

## Requirements

- React Native 0.60+
- iOS 11.0+
- Android API Level 21+

## Testing

Before using in production, test the library with real video files:

```bash
# Create test video files (requires FFmpeg)
./scripts/create-test-videos.sh

# Run example app for interactive testing
yarn example:ios     # iOS
yarn example:android # Android

# Run unit tests
yarn test
```

See [TESTING.md](TESTING.md) for comprehensive testing guide.

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
