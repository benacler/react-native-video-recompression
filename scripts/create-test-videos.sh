#!/bin/bash

# Script to create test video files for development
# Requires FFmpeg to be installed

ASSETS_DIR="./example/assets"
mkdir -p "$ASSETS_DIR"

echo "Creating test video files..."

# Create a 5-second test pattern video (H.264, 720p)
if command -v ffmpeg &> /dev/null; then
    echo "📹 Creating test-h264.mp4..."
    ffmpeg -f lavfi -i "testsrc2=duration=5:size=1280x720:rate=30" \
           -f lavfi -i "sine=frequency=1000:duration=5" \
           -c:v libx264 -preset fast -pix_fmt yuv420p \
           -c:a aac -shortest \
           "$ASSETS_DIR/test-h264.mp4" -y -loglevel quiet

    echo "📹 Creating test-sample.mov..."
    ffmpeg -i "$ASSETS_DIR/test-h264.mp4" -c copy \
           "$ASSETS_DIR/test-sample.mov" -y -loglevel quiet

    echo "📹 Creating test-high-bitrate.mp4..."
    ffmpeg -i "$ASSETS_DIR/test-h264.mp4" -b:v 5M -maxrate 5M -bufsize 10M \
           "$ASSETS_DIR/test-high-bitrate.mp4" -y -loglevel quiet

    echo "📹 Creating test-vertical.mp4..."
    ffmpeg -f lavfi -i "testsrc2=duration=5:size=720x1280:rate=30" \
           -f lavfi -i "sine=frequency=800:duration=5" \
           -c:v libx264 -preset fast -pix_fmt yuv420p \
           -c:a aac -shortest \
           "$ASSETS_DIR/test-vertical.mp4" -y -loglevel quiet

    echo "📹 Creating test-small.mp4..."
    ffmpeg -i "$ASSETS_DIR/test-h264.mp4" -s 640x360 -b:v 500k \
           "$ASSETS_DIR/test-small.mp4" -y -loglevel quiet

    echo "✅ Test video files created successfully!"
    echo "📁 Location: $ASSETS_DIR"
    echo ""
    echo "Files created:"
    ls -lh "$ASSETS_DIR"/*.mp4 "$ASSETS_DIR"/*.mov 2>/dev/null || true
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
echo "  yarn example:ios    # Test on iOS"
echo "  yarn example:android # Test on Android"
