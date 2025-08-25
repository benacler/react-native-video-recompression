# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.0.0] - 2024-08-25

### Added
- Initial release of react-native-video-recompression
- **Smart Video Processing**: Automatic strategy selection (passthrough/rewrap/recompress)
- **Video Analysis**: Comprehensive video metadata extraction and codec detection
- **Quality Preservation**: Lossless format conversion when recompression isn't needed
- **Custom Compression**: Fine-tunable quality, bitrate, resolution, and codec settings
- **Progress Tracking**: Real-time progress callbacks for long-running operations
- Cross-platform support (iOS and Android) with native performance
- TypeScript support with full type definitions
- Native implementations:
  - iOS: Uses AVFoundation with intelligent export preset selection
  - Android: Uses MediaMuxer, MediaExtractor, and MediaCodec with optimal strategy selection
- Background processing to avoid UI blocking
- Comprehensive error handling and reporting
- Support for various video formats and codecs
- Audio track preservation during all processing modes
- React Native autolinking support

### Core Features
- ✅ **Video Analysis**: Extract detailed metadata without modification
- ✅ **Smart Processing**: Automatic optimization strategy selection
- ✅ **Quality Preservation**: Lossless rewrapping when appropriate
- ✅ **Custom Compression**: User-controlled quality and format settings
- ✅ **Progress Callbacks**: Real-time processing updates
- ✅ **Cross Platform**: Native iOS 11.0+ and Android API 21+ support
- ✅ **Background Processing**: Non-blocking operations
- ✅ **TypeScript Support**: Full type safety and IntelliSense

### Processing Strategies
1. **Passthrough**: File is already optimal, no changes made
2. **Rewrap**: Changes container format while preserving video/audio quality  
3. **Recompress**: Applies compression settings to reduce file size or change quality

### Technical Implementation
- iOS: AVAssetExportSession with smart preset selection and custom export settings
- Android: MediaMuxer/MediaExtractor with MediaCodec integration and Kotlin coroutines
- Memory-efficient processing for large video files
- Automatic codec detection and compatibility checking
