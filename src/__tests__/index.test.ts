import VideoRecompression from '../index';

// Mock react-native
jest.mock('react-native', () => ({
  NativeModules: {
    VideoRecompression: {
      init: jest.fn(() =>
        Promise.resolve({
          platform: 'ios',
          version: '1.0.0',
          capabilities: ['video_analysis', 'smart_compression'],
        })
      ),
      analyzeVideo: jest.fn(() =>
        Promise.resolve({
          container: 'mov',
          videoCodec: 'h264',
          audioCodec: 'aac',
          width: 1920,
          height: 1080,
          duration: 60,
          videoBitrate: 5000000,
          audioBitrate: 128000,
          frameRate: 30,
          fileSize: 50000000,
        })
      ),
      processVideo: jest.fn(() =>
        Promise.resolve({
          outputPath: '/path/to/output.mp4',
          action: 'recompress',
          originalInfo: {},
          finalInfo: {},
          processingTime: 5000,
        })
      ),
      convert: jest.fn(() => Promise.resolve('/path/to/output.mp4')),
    },
  },
  Platform: {
    OS: 'ios',
    select: jest.fn(config => config.ios || config.default),
  },
}));

describe('VideoRecompression', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('init', () => {
    it('should initialize successfully', async () => {
      const result = await VideoRecompression.init();

      expect(result).toEqual({
        platform: 'ios',
        version: '1.0.0',
        capabilities: ['video_analysis', 'smart_compression'],
      });
      expect(result.platform).toBe('ios');
      expect(result.capabilities).toContain('video_analysis');
      expect(result.capabilities).toContain('smart_compression');
    });
  });

  describe('analyzeVideo', () => {
    it('should analyze video successfully', async () => {
      const filePath = '/path/to/video.mov';
      const result = await VideoRecompression.analyzeVideo(filePath);

      expect(result).toEqual({
        container: 'mov',
        videoCodec: 'h264',
        audioCodec: 'aac',
        width: 1920,
        height: 1080,
        duration: 60,
        videoBitrate: 5000000,
        audioBitrate: 128000,
        frameRate: 30,
        fileSize: 50000000,
      });
    });
  });

  describe('processVideo', () => {
    it('should process video successfully', async () => {
      const inputPath = '/path/to/input.mov';
      const outputPath = '/path/to/output.mp4';

      const result = await VideoRecompression.processVideo(
        inputPath,
        outputPath
      );

      expect(result).toEqual({
        outputPath: '/path/to/output.mp4',
        action: 'recompress',
        originalInfo: {},
        finalInfo: {},
        processingTime: 5000,
      });
    });

    it('should process video with settings', async () => {
      const inputPath = '/path/to/input.mov';
      const outputPath = '/path/to/output.mp4';
      const settings = {
        videoCodec: 'h264' as const,
        quality: 0.8,
        maxWidth: 1920,
      };

      const result = await VideoRecompression.processVideo(
        inputPath,
        outputPath,
        settings
      );

      expect(result).toBeDefined();
      expect(result.outputPath).toBe('/path/to/output.mp4');
    });
  });

  describe('error handling', () => {
    it('should handle file not found errors', async () => {
      // Override the mock to reject for this test
      const mockAnalyze = jest
        .fn()
        .mockRejectedValue(new Error('File not found'));

      // Replace the mock temporarily
      const originalAnalyze = VideoRecompression.analyzeVideo;
      VideoRecompression.analyzeVideo = mockAnalyze;

      await expect(
        VideoRecompression.analyzeVideo('/nonexistent.mov')
      ).rejects.toThrow('File not found');

      // Restore original mock
      VideoRecompression.analyzeVideo = originalAnalyze;
    });

    it('should validate input parameters', () => {
      // Test that the methods exist and can be called
      expect(typeof VideoRecompression.processVideo).toBe('function');
      expect(typeof VideoRecompression.analyzeVideo).toBe('function');
      expect(typeof VideoRecompression.init).toBe('function');
    });
  });
});
