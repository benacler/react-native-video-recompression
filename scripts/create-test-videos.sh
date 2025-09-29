#!/bin/bash

# Script to create test video files for development
# Requires FFmpeg to be installed

ASSETS_DIR="./test-assets"
mkdir -p "$ASSETS_DIR"

echo "Creating test video files for video recompression testing..."

# Create a 5-second test pattern video (H.264, 720p)
if command -v ffmpeg &> /dev/null; then
    echo "📹 Creating test-h264-720p.mp4 (standard quality)..."
    ffmpeg -f lavfi -i "testsrc2=duration=10:size=1280x720:rate=30" \
           -f lavfi -i "sine=frequency=1000:duration=10" \
           -c:v libx264 -preset fast -pix_fmt yuv420p -b:v 2000k \
           -c:a aac -b:a 128k -shortest \
           "$ASSETS_DIR/test-h264-720p.mp4" -y -loglevel quiet

    echo "📹 Creating test-h264-1080p.mp4 (high resolution)..."
    ffmpeg -f lavfi -i "testsrc2=duration=10:size=1920x1080:rate=30" \
           -f lavfi -i "sine=frequency=800:duration=10" \
           -c:v libx264 -preset fast -pix_fmt yuv420p -b:v 4000k \
           -c:a aac -b:a 192k -shortest \
           "$ASSETS_DIR/test-h264-1080p.mp4" -y -loglevel quiet

    echo "📹 Creating test-high-bitrate.mp4 (needs compression)..."
    ffmpeg -f lavfi -i "testsrc2=duration=8:size=1920x1080:rate=30" \
           -f lavfi -i "sine=frequency=600:duration=8" \
           -c:v libx264 -preset fast -pix_fmt yuv420p -b:v 8000k -maxrate 8000k -bufsize 16M \
           -c:a aac -b:a 256k -shortest \
           "$ASSETS_DIR/test-high-bitrate.mp4" -y -loglevel quiet

    echo "📹 Creating test-vertical.mp4 (portrait mode)..."
    ffmpeg -f lavfi -i "testsrc2=duration=6:size=720x1280:rate=30" \
           -f lavfi -i "sine=frequency=1200:duration=6" \
           -c:v libx264 -preset fast -pix_fmt yuv420p -b:v 1500k \
           -c:a aac -b:a 128k -shortest \
           "$ASSETS_DIR/test-vertical.mp4" -y -loglevel quiet

    echo "📹 Creating test-mov-format.mov (different container)..."
    ffmpeg -i "$ASSETS_DIR/test-h264-720p.mp4" -c copy \
           "$ASSETS_DIR/test-mov-format.mov" -y -loglevel quiet

    echo "📹 Creating test-4k.mp4 (ultra high resolution)..."
    ffmpeg -f lavfi -i "testsrc2=duration=5:size=3840x2160:rate=24" \
           -f lavfi -i "sine=frequency=400:duration=5" \
           -c:v libx264 -preset fast -pix_fmt yuv420p -b:v 15000k \
           -c:a aac -b:a 192k -shortest \
           "$ASSETS_DIR/test-4k.mp4" -y -loglevel quiet

    echo "📹 Creating test-small.mp4 (low resolution)..."
    ffmpeg -f lavfi -i "testsrc2=duration=8:size=640x360:rate=30" \
           -f lavfi -i "sine=frequency=1500:duration=8" \
           -c:v libx264 -preset fast -pix_fmt yuv420p -b:v 500k \
           -c:a aac -b:a 96k -shortest \
           "$ASSETS_DIR/test-small.mp4" -y -loglevel quiet

    echo "📹 Creating test-webm.webm (VP8 codec)..."
    ffmpeg -i "$ASSETS_DIR/test-h264-720p.mp4" \
           -c:v libvpx -b:v 1000k -c:a libvorbis -b:a 128k \
           "$ASSETS_DIR/test-webm.webm" -y -loglevel quiet

    # Create a test configuration file
    cat > "$ASSETS_DIR/test-config.json" << EOF
{
  "testFiles": [
    {
      "name": "test-h264-720p.mp4",
      "description": "Standard 720p H.264 video, good baseline",
      "expectedAction": "passthrough",
      "resolution": "1280x720",
      "codec": "h264",
      "container": "mp4"
    },
    {
      "name": "test-h264-1080p.mp4", 
      "description": "1080p H.264 video, may need resize",
      "expectedAction": "recompress",
      "resolution": "1920x1080",
      "codec": "h264",
      "container": "mp4"
    },
    {
      "name": "test-high-bitrate.mp4",
      "description": "High bitrate video requiring compression",
      "expectedAction": "recompress",
      "resolution": "1920x1080",
      "codec": "h264",
      "container": "mp4"
    },
    {
      "name": "test-vertical.mp4",
      "description": "Portrait orientation video",
      "expectedAction": "recompress",
      "resolution": "720x1280",
      "codec": "h264",
      "container": "mp4"
    },
    {
      "name": "test-mov-format.mov",
      "description": "MOV container, should rewrap to MP4",
      "expectedAction": "rewrap",
      "resolution": "1280x720",
      "codec": "h264",
      "container": "mov"
    },
    {
      "name": "test-4k.mp4",
      "description": "4K video, definitely needs compression",
      "expectedAction": "recompress",
      "resolution": "3840x2160",
      "codec": "h264",
      "container": "mp4"
    },
    {
      "name": "test-small.mp4",
      "description": "Small resolution, likely passthrough",
      "expectedAction": "passthrough",
      "resolution": "640x360", 
      "codec": "h264",
      "container": "mp4"
    },
    {
      "name": "test-webm.webm",
      "description": "WebM/VP8 format, needs transcode to H.264",
      "expectedAction": "recompress",
      "resolution": "1280x720",
      "codec": "vp8",
      "container": "webm"
    }
  ],
  "testSettings": {
    "audioBitrate": 128000,
    "audioCodec": "aac",
    "maxHeight": 720,
    "maxWidth": 1280,
    "optimizeForNetwork": true,
    "quality": 0.7,
    "videoBitrate": 800000,
    "videoCodec": "h264"
  }
}
EOF

    echo "✅ Test video files created successfully!"
    echo "📁 Location: $ASSETS_DIR"
    echo ""
    echo "Files created:"
    ls -lh "$ASSETS_DIR"/*.mp4 "$ASSETS_DIR"/*.mov "$ASSETS_DIR"/*.webm 2>/dev/null || true
    echo ""
    echo "📋 Test configuration saved to: $ASSETS_DIR/test-config.json"
else
    echo "❌ FFmpeg not found. Please install FFmpeg to create test videos."
    echo ""
    echo "Install FFmpeg:"
    echo "  macOS: brew install ffmpeg"
    echo "  Ubuntu: sudo apt install ffmpeg"
    echo "  Windows: Download from https://ffmpeg.org/"
    echo ""
    echo "Alternatively, manually add test video files to: $ASSETS_DIR"
fi

echo ""
echo "🧪 Ready for testing! Run:"
echo "  ./scripts/setup-android-test.sh   # Setup Android emulator"
echo "  ./scripts/start-android-test.sh   # Start emulator"
echo "  ./scripts/run-video-tests.sh      # Run comprehensive tests"
