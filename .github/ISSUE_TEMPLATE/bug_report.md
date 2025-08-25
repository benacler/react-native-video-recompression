---
name: Bug report
about: Create a report to help us improve
title: '[BUG] '
labels: 'bug'
assignees: ''
---

**Describe the bug**
A clear and concise description of what the bug is.

**To Reproduce**
Steps to reproduce the behavior:
1. Call method '...' (e.g., analyzeVideo, processVideo, init)
2. Pass parameters '...'
3. Expected behavior '...'
4. See error

**Method being used**
- [ ] `init()` - Module initialization
- [ ] `analyzeVideo()` - Video analysis/metadata extraction  
- [ ] `processVideo()` - Smart video processing

**Processing details (if using processVideo)**
- Processing strategy expected: [passthrough/rewrap/recompress]
- Compression settings used: [quality, maxWidth, videoCodec, etc.]
- Progress callback: [yes/no]

**Expected behavior**
A clear and concise description of what you expected to happen.

**Error details**
```
Paste the complete error message here
```

**Environment (please complete the following information):**
- React Native version: [e.g. 0.72.7]
- Platform: [e.g. iOS 16.1, Android API 33]
- Device: [e.g. iPhone 14, Samsung Galaxy S22]
- Package version: [e.g. 1.0.0]

**Video details (if applicable):**
- Input format: [e.g. MP4, AVI, WEBM]
- Input codec: [e.g. H.264, HEVC]
- Input resolution: [e.g. 1920x1080]
- Input file size: [e.g. 50MB]

**Additional context**
Add any other context about the problem here, such as:
- Does it happen with all videos or specific ones?
- Are you using any specific compression settings?
- Any relevant logs from iOS/Android native side
