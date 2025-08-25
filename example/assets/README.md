# Test Video Files

This directory should contain test video files for development and testing.

## Recommended Test Files

You should add these test video files (keep them small for repository size):

### Basic Test Files (< 1MB each)
- `test-h264.mp4` - H.264 encoded MP4 file
- `test-hevc.mp4` - HEVC/H.265 encoded MP4 file  
- `test-sample.mov` - MOV container with H.264
- `test-different-resolution.mp4` - Different resolution (e.g., 720p)

### Advanced Test Files
- `test-high-bitrate.mp4` - High bitrate file for compression testing
- `test-vertical.mp4` - Vertical/portrait video
- `test-audio-only.mp4` - File with audio track issues
- `test-corrupted.mp4` - Intentionally corrupted file for error testing

## How to Create Test Files

You can create small test files using FFmpeg:

```bash
# Create a 5-second H.264 MP4 test file
ffmpeg -f lavfi -i testsrc2=duration=5:size=1280x720:rate=30 -c:v libx264 -preset fast test-h264.mp4

# Create a MOV version
ffmpeg -i test-h264.mp4 -c copy test-sample.mov

# Create a high bitrate version
ffmpeg -i test-h264.mp4 -b:v 5M test-high-bitrate.mp4

# Create a vertical video
ffmpeg -f lavfi -i testsrc2=duration=5:size=720x1280:rate=30 -c:v libx264 test-vertical.mp4
```

## Alternative Sources

If you don't have FFmpeg, you can:

1. **Download from free sources:**
   - [Sample Videos](https://sample-videos.com/) - Free test video files
   - [Pixabay](https://pixabay.com/videos/) - Free stock videos
   - [Pexels](https://www.pexels.com/videos/) - Free stock videos

2. **Record from device:**
   - Use your phone to record short (5-10 second) test videos
   - Try different formats if your device supports it

3. **Use online converters:**
   - Convert between formats using online tools
   - Create different quality versions

## Important Notes

- Keep files small (< 1-2MB) to avoid bloating the repository
- Include various formats, resolutions, and codecs
- Don't commit copyrighted content
- Add larger test files to `.gitignore` if needed
