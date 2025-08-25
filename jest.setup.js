// Mock React Native modules
jest.mock('react-native', () => ({
  NativeModules: {
    VideoRecompression: {
      init: jest.fn(() => Promise.resolve({ 
        platform: 'ios', 
        version: '1.0.0', 
        capabilities: [] 
      })),
      analyzeVideo: jest.fn(() => Promise.resolve({})),
      processVideo: jest.fn(() => Promise.resolve({ outputPath: '/path/to/output.mp4' })),
    },
  },
  Platform: {
    OS: 'ios',
    select: jest.fn((config) => config.ios || config.default),
  },
}));
