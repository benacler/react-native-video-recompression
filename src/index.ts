import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
    `The package 'react-native-video-recompression' doesn't seem to be linked. Make sure: \n\n` +
    Platform.select({ ios: "- You have run 'cd ios && pod install'\n", default: '' }) +
    '- You rebuilt the app after installing the package\n' +
    '- You are not using Expo Go\n';

const VideoRecompression = NativeModules.VideoRecompression
    ? NativeModules.VideoRecompression
    : new Proxy(
        {},
        {
            get() {
                throw new Error(LINKING_ERROR);
            },
        }
    );

export interface VideoInfo {
    /** Container format (mp4, mov, avi, etc.) */
    container: string;
    /** Video codec (h264, hevc, vp9, etc.) */
    videoCodec: string;
    /** Audio codec (aac, mp3, pcm, etc.) */
    audioCodec: string;
    /** Video width in pixels */
    width: number;
    /** Video height in pixels */
    height: number;
    /** Duration in seconds */
    duration: number;
    /** Video bitrate in bits per second */
    videoBitrate: number;
    /** Audio bitrate in bits per second */
    audioBitrate: number;
    /** Frame rate */
    frameRate: number;
    /** File size in bytes */
    fileSize: number;
}

export interface CompressionSettings {
    /** Target video codec (h264, hevc) */
    videoCodec?: 'h264' | 'hevc';
    /** Target audio codec (aac, mp3) */
    audioCodec?: 'aac' | 'mp3';
    /** Target video bitrate in bits per second */
    videoBitrate?: number;
    /** Target audio bitrate in bits per second */
    audioBitrate?: number;
    /** Target maximum width */
    maxWidth?: number;
    /** Target maximum height */
    maxHeight?: number;
    /** Target frame rate */
    frameRate?: number;
    /** Compression quality (0.0 to 1.0) */
    quality?: number;
    /** Whether to optimize for network use */
    optimizeForNetwork?: boolean;
}

export interface CompressionResult {
    /** Output file path */
    outputPath: string;
    /** Action taken (passthrough, rewrap, recompress) */
    action: 'passthrough' | 'rewrap' | 'recompress';
    /** Original file info */
    originalInfo: VideoInfo;
    /** Final file info */
    finalInfo: VideoInfo;
    /** Time taken in milliseconds */
    processingTime: number;
}

export interface VideoRecompressionInterface {
    /**
     * Initialize the video recompression module and test connectivity
     * @returns Promise that resolves to module information including platform and capabilities
     */
    init(): Promise<{
        platform: 'ios' | 'android';
        version: string;
        capabilities: string[];
    }>;

    /**
     * Analyze a video file and extract comprehensive metadata information
     * 
     * This method provides detailed technical information about a video file without
     * modifying it, including codec information, dimensions, bitrates, and duration.
     * 
     * @param filePath Absolute path to the video file to analyze
     * @returns Promise that resolves to comprehensive video information
     */
    analyzeVideo(filePath: string): Promise<VideoInfo>;

    /**
     * Smart video processing with automatic strategy selection
     * 
     * This method intelligently determines the best processing approach:
     * - **Passthrough**: File is already optimal, no processing needed
     * - **Rewrap**: Change container format while preserving video/audio quality
     * - **Recompress**: Apply compression settings to reduce size or change quality
     * 
     * @param inputPath Absolute path to the input video file
     * @param outputPath Absolute path for the output file
     * @param settings Optional compression settings to customize output
     * @param onProgress Optional progress callback function (receives values from 0.0 to 1.0)
     * @returns Promise that resolves to processing result with details about the operation performed
     */
    processVideo(
        inputPath: string,
        outputPath: string,
        settings?: CompressionSettings,
        onProgress?: (progress: number) => void
    ): Promise<CompressionResult>;
}

export default VideoRecompression as VideoRecompressionInterface;
