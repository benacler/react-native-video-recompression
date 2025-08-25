# Testing Guide

This guide explains how to test the react-native-video-recompression library before publishing.

## 🧪 Testing Strategy

### 1. **Unit Tests** ✅
```bash
yarn test
```
- Mocked tests for TypeScript interfaces
- Basic functionality validation
- Error handling tests

### 2. **Integration Tests** (Manual)
- Real video file processing
- Native module functionality
- Cross-platform compatibility

### 3. **Example App Testing** (Recommended)
- Interactive testing with real videos
- All API methods validation
- Progress callback testing

## 🚀 Quick Start Testing

### Prerequisites
- React Native development environment set up
- iOS Simulator or Android Emulator/Device
- Test video files (see `example/assets/README.md`)

### Option 1: Example App (Recommended)

```bash
# 1. Navigate to example directory
cd example

# 2. Install dependencies
yarn install

# 3. Install iOS dependencies (iOS only)
cd ios && pod install && cd ..

# 4. Run on iOS
yarn ios

# 5. Run on Android
yarn android
```

### Option 2: Create Your Own Test App

```bash
# 1. Create new RN app
npx react-native@latest init VideoTest

# 2. Install the library
cd VideoTest
yarn add file:../react-native-video-recompression

# 3. Link native dependencies
cd ios && pod install && cd .. # iOS
# Android auto-links

# 4. Import and test in your app
```

## 📱 Manual Testing Checklist

### ✅ **Initialization Testing**
- [ ] Module initializes successfully
- [ ] Returns correct platform info (iOS/Android)
- [ ] Returns version and capabilities

### ✅ **Video Analysis Testing**
- [ ] Analyzes MP4 files correctly
- [ ] Analyzes MOV files correctly
- [ ] Handles different video codecs (H.264, HEVC)
- [ ] Extracts correct metadata (resolution, duration, bitrate)
- [ ] Handles audio track information
- [ ] Fails gracefully with invalid files

### ✅ **Video Processing Testing**
- [ ] **Passthrough**: Detects when no processing needed
- [ ] **Rewrap**: Changes container while preserving quality
- [ ] **Recompress**: Applies compression settings correctly
- [ ] Progress callbacks work (0.0 to 1.0)
- [ ] Handles different input formats
- [ ] Respects custom compression settings
- [ ] Output files are playable

### ✅ **Error Handling Testing**
- [ ] Invalid file paths handled gracefully
- [ ] Corrupted files handled gracefully
- [ ] Permission issues handled
- [ ] Network/storage issues handled
- [ ] Meaningful error messages provided

### ✅ **Performance Testing**
- [ ] Large files (>50MB) process without crashes
- [ ] Multiple concurrent operations handled
- [ ] Memory usage remains stable
- [ ] Background processing doesn't block UI

### ✅ **Cross-Platform Testing**
- [ ] iOS implementation works correctly
- [ ] Android implementation works correctly
- [ ] Consistent behavior across platforms
- [ ] Platform-specific optimizations work

## 🔧 Test Video Scenarios

### Basic Functionality
1. **MP4 → MP4 (same codec)**: Should passthrough
2. **MOV → MP4 (same codec)**: Should rewrap
3. **High bitrate → Low bitrate**: Should recompress
4. **Large resolution → Small resolution**: Should recompress

### Edge Cases
1. **Very short video (< 1 second)**
2. **Very long video (> 5 minutes)**
3. **Vertical/portrait video**
4. **Audio-only file**
5. **No audio track**
6. **Unusual aspect ratios**

### Error Cases
1. **Non-existent file path**
2. **Corrupted video file**
3. **Unsupported format**
4. **Read-only output directory**
5. **Insufficient storage space**

## 📊 Performance Benchmarks

Track these metrics during testing:

```typescript
// Example performance logging
const startTime = Date.now();
const result = await VideoRecompression.processVideo(inputPath, outputPath);
const processingTime = Date.now() - startTime;

console.log(`
Performance Metrics:
- Input size: ${inputSize}MB
- Output size: ${outputSize}MB  
- Processing time: ${processingTime}ms
- Compression ratio: ${inputSize/outputSize}x
- Strategy used: ${result.action}
`);
```

## 🐛 Common Issues & Solutions

### iOS Issues
- **Build errors**: Make sure `pod install` was run
- **Linking errors**: Check React Native version compatibility
- **Permission errors**: Add camera/photo library permissions

### Android Issues
- **Build errors**: Check Android SDK and gradle versions
- **File access errors**: Check storage permissions
- **Performance issues**: Test on physical device, not emulator

### General Issues
- **Large files**: Test memory usage and timeout handling
- **Network files**: Test with remote video URLs if supported
- **Concurrent operations**: Test multiple simultaneous processes

## ✅ Pre-Publishing Checklist

Before publishing to npm:

- [ ] All unit tests pass (`yarn test`)
- [ ] TypeScript compilation works (`yarn typecheck`)
- [ ] Linting passes (`yarn lint`)
- [ ] Example app builds and runs on iOS
- [ ] Example app builds and runs on Android
- [ ] Manual testing completed with various video formats
- [ ] Performance testing completed
- [ ] Error handling verified
- [ ] Documentation updated
- [ ] Version number updated
- [ ] CHANGELOG updated

## 📝 Reporting Issues

When reporting bugs during testing:

1. Include platform (iOS/Android) and version
2. Include video file details (format, codec, size)
3. Include exact error messages
4. Include steps to reproduce
5. Include expected vs actual behavior

Use the GitHub issue template for consistent reporting.
