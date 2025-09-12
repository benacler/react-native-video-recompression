# react-native-video-recompression

[![npm version](https://badge.fury.io/js/react-native-video-recompression.svg)](https://badge.fury.io/js/react-native-video-recompression)
[![npm downloads](https://img.shields.io/npm/dm/react-native-video-recompression.svg)](https://npmjs.org/package/react-native-video-recompression)
[![CI](https://github.com/benacler/react-native-video-recompression/workflows/CI/badge.svg)](https://github.com/benacler/react-native-video-recompression/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

A React Native library for **intelligent video processing** with native performance. Features **accurate bitrate analysis**, **smart MOV→MP4 rewrapping**, and **bitrate-aware compression decisions** optimized for mobile chat applications and media sharing.

## 🚀 Key Features

- 🧠 **Smart Processing**: Intelligent decision engine chooses optimal strategy (passthrough/rewrap/recompress)
- 📊 **Accurate Bitrate Analysis**: Real track-level bitrate detection, not file-size estimates
- 📱 **iOS Camera Roll Optimization**: Fast MOV→MP4 rewrapping for iPhone videos
- ⚡ **Performance First**: Container rewrapping vs slow transcoding when possible
- 🎯 **Chat App Ready**: Bitrate-aware decisions for messaging apps (2Mbps/192kbps thresholds)
- 📹 **Enhanced Codec Detection**: H.264, HEVC, VP8, VP9, AV1, AAC, MP3, Opus support
- 🔍 **Deep Video Analysis**: Comprehensive metadata extraction with format details
- 📱 **Cross Platform**: Consistent behavior across iOS and Android

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

## 🧪 Testing

### Unit Tests
```bash
npm test
```

### Build Validation  
```bash
npm run build
npm run validate
```

### Integration Testing
Create a new React Native app and install the library:

```bash
# Create new RN app
npx react-native@0.72.7 init VideoTestApp

# Install the library
cd VideoTestApp
npm install react-native-video-recompression

# iOS setup
cd ios && pod install && cd ..

# Test on devices
npm run ios
npm run android
```

For detailed testing instructions, see [TESTING.md](TESTING.md).

## Usage

```javascript
import VideoRecompression from 'react-native-video-recompression';

// Initialize and test the module
const initializeModule = async () => {
  try {
    const info = await VideoRecompression.init();
    console.log('Module initialized:', info);
    // Output: { platform: 'ios', version: '0.9.4', capabilities: ['video_analysis', 'smart_compression', ...] }
  } catch (error) {
    console.error('Module initialization failed:', error);
  }
};

// Analyze video file with accurate bitrate detection
const analyzeVideo = async (videoPath) => {
  try {
    const videoInfo = await VideoRecompression.analyzeVideo(videoPath);
    console.log('Video analysis:', videoInfo);
    // Output: { 
    //   container: 'mov', 
    //   videoCodec: 'h264', 
    //   audioCodec: 'aac',
    //   width: 1920, height: 1080, duration: 30.5,
    //   videoBitrate: 1800000,  // Actual bitrate from track metadata
    //   audioBitrate: 128000,   // Actual bitrate from track metadata
    //   frameRate: 30.0, fileSize: 45000000
    // }
  } catch (error) {
    console.error('Video analysis failed:', error);
  }
};

// Smart video processing for chat apps (iPhone MOV → MP4 example)
const processVideoForChat = async (inputPath, outputPath) => {
  try {
    const result = await VideoRecompression.processVideo(
      inputPath,  // '/path/to/iPhone_video.mov'
      outputPath, // '/path/to/chat_optimized.mp4'
      {
        // Chat optimization settings
        videoBitrate: 800000,    // 800kbps for messaging
        audioBitrate: 128000,    // 128kbps for messaging
        videoCodec: 'h264',      // Universal compatibility
        audioCodec: 'aac',       // Universal compatibility
        maxWidth: 1280,          // Limit resolution for chat
        maxHeight: 720,
        optimizeForNetwork: true
      },
      (progress) => {
        console.log(`Processing: ${Math.round(progress * 100)}%`);
      }
    );
    
    console.log('Processing result:', result);
    // For iPhone MOV with H.264+AAC and reasonable bitrate:
    // { 
    //   outputPath: '/path/to/chat_optimized.mp4',
    //   action: 'rewrap',           // Fast container conversion!
    //   processingTime: 2500,       // ~2.5 seconds vs 30+ for recompression
    //   originalInfo: { fileSize: 45000000, videoBitrate: 1800000 },
    //   finalInfo: { fileSize: 45000000, videoBitrate: 1800000 }  // Same quality
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
  - `videoCodec`: Video codec (h264, hevc, vp8, vp9, av1, etc.)
  - `audioCodec`: Audio codec (aac, mp3, opus, vorbis, flac, etc.)
  - `width`, `height`: Video dimensions
  - `duration`: Duration in seconds
  - `videoBitrate`, `audioBitrate`: **Accurate bitrates** from track metadata (not estimates)
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
- **Passthrough**: File already meets target requirements (codecs + bitrates optimal)
- **Rewrap**: Fast container conversion (MOV→MP4) preserving video/audio quality  
- **Recompress**: Full transcoding when bitrates exceed thresholds or wrong codecs

**Smart Decision Logic:**
- **Video Threshold**: 2 Mbps (recompress if higher for chat optimization)
- **Audio Threshold**: 192 kbps (recompress if higher for chat optimization)
- **iPhone MOV Files**: H.264+AAC with reasonable bitrate → **rewrap** to MP4 (seconds vs minutes)
- **High Bitrate Videos**: Automatic recompression with target settings
- **Already Optimal**: Instant passthrough with file copy

## 🎯 **Chat Application Use Cases**

Perfect for messaging apps like WhatsApp, Telegram, or custom chat applications:

```javascript
// iPhone camera roll video optimization
const optimizeForChat = async (cameraRollVideoPath) => {
  const analysis = await VideoRecompression.analyzeVideo(cameraRollVideoPath);
  
  // Typical iPhone video: MOV container, H.264 video, AAC audio
  if (analysis.container === 'mov' && 
      analysis.videoCodec === 'h264' && 
      analysis.videoBitrate <= 2000000) {
    
    // Result: Fast rewrap (2-5 seconds) instead of slow recompression (30+ seconds)
    const result = await VideoRecompression.processVideo(input, output, {
      videoBitrate: 800000,  // 800kbps for chat
      audioBitrate: 128000   // 128kbps for chat  
    });
    
    console.log(result.action); // 'rewrap' - preserves quality, changes container
  }
};
```

## Features

- 🧠 **Smart Bitrate-Aware Processing**: Decisions based on actual track bitrates, not file size estimates
- 📱 **iPhone MOV Optimization**: Fast rewrapping for iOS camera roll videos (MOV→MP4)  
- ⚡ **Performance Optimized**: Container rewrapping (seconds) vs full transcoding (minutes)
- 🎯 **Chat App Ready**: Pre-configured thresholds for messaging applications (2Mbps/192kbps)
- 📊 **Accurate Video Analysis**: Real bitrate detection using MediaExtractor (Android) & AVAssetTrack (iOS)
- 🔍 **Enhanced Codec Detection**: H.264, HEVC, VP8, VP9, AV1, AAC, MP3, Opus, Vorbis, FLAC
- ✅ **Quality Preservation**: Lossless format conversion when recompression isn't needed
- ⚙️ **Custom Compression**: Fine-tune quality, bitrate, resolution, and codec settings
- 📈 **Progress Callbacks**: Real-time progress updates for long-running operations
- 📱 **Cross Platform**: Consistent behavior and feature parity across iOS and Android
- 🔄 **Background Processing**: Non-blocking operations using background threads/queues
- 🔗 **Zero Configuration**: Works out of the box with React Native autolinking
- 📘 **TypeScript Support**: Full TypeScript definitions included
- 📹 **Multiple Formats**: Support for MP4, MOV, AVI, WEBM, and other common video formats

## Use Cases

- **📱 Chat Applications**: Optimize iPhone MOV videos for messaging with fast rewrapping
- **🔄 Format Conversion**: Convert between video formats while preserving quality  
- **📉 File Size Optimization**: Reduce video file sizes for storage or network transmission
- **📊 Video Analysis**: Extract accurate metadata and bitrate information from video files
- **⚙️ Quality Adjustment**: Change video quality, resolution, or codec for specific requirements
- **📁 Batch Processing**: Process multiple videos with consistent settings and smart decisions

## Technical Details

### iOS Implementation
- Uses `AVFoundation` framework with `AVAssetExportSession` and `AVAssetTrack`
- **Accurate Bitrate Detection**: Uses `estimatedDataRate` property from tracks (not file-size estimation)
- **Smart Strategy Selection**:
  - **Passthrough**: File already meets target codec and bitrate requirements
  - **Rewrap**: Uses `AVAssetExportPresetPassthrough` for fast container conversion (MOV→MP4)
  - **Recompress**: Uses quality presets when bitrates exceed thresholds (2Mbps video, 192kbps audio)
- **Enhanced Codec Detection**: CMFormatDescription analysis for H.264, HEVC, VP8, VP9, AV1, AAC, MP3, Opus
- **Decision Logging**: Comprehensive logging of bitrate analysis and processing decisions
- Background processing on dedicated queues to avoid UI blocking

### Android Implementation  
- Uses `MediaExtractor`, `MediaMuxer`, and `MediaMetadataRetriever` for comprehensive analysis
- **Accurate Bitrate Detection**: MediaExtractor track-level analysis using `MediaFormat.KEY_BIT_RATE`
- **Smart Strategy Selection**:
  - **Passthrough**: File meets target requirements (codecs + bitrates within thresholds)
  - **Rewrap**: Container format change using MediaMuxer without reencoding (MOV→MP4)
  - **Recompress**: Full transcoding with MediaCodec when bitrates exceed chat thresholds
- **Enhanced Codec Detection**: MIME-type analysis for H.264, HEVC, VP8, VP9, AV1, AAC, MP3, Opus, Vorbis, FLAC
- **Decision Logging**: Detailed bitrate analysis and processing strategy logging
- Kotlin coroutines for asynchronous background processing

### Performance Optimizations
- **Smart Decision Engine**: Bitrate-aware processing prevents unnecessary recompression
- **Fast MOV→MP4 Rewrapping**: Container conversion in seconds vs transcoding in minutes
- **Lazy Loading**: Efficient native module initialization
- **Memory-Efficient Streaming**: Optimized for large video files without memory spikes
- **Automatic Resource Cleanup**: Proper disposal of MediaExtractor, AVAsset, and codec resources
- **Track-Level Analysis**: Direct format inspection instead of full file processing

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
