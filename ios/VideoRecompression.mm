#import "VideoRecompression.h"
#import <React/RCTLog.h>
#import <React/RCTEventEmitter.h>
#import <AVFoundation/AVFoundation.h>
#import <VideoToolbox/VideoToolbox.h>

@interface VideoRecompression ()
@property (nonatomic, strong) NSMutableDictionary *progressCallbacks;
@end

@implementation VideoRecompression

RCT_EXPORT_MODULE()

- (instancetype)init {
    if (self = [super init]) {
        _progressCallbacks = [NSMutableDictionary dictionary];
    }
    return self;
}

RCT_EXPORT_METHOD(init:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject)
{
    dispatch_async(dispatch_get_main_queue(), ^{
        RCTLogInfo(@"VideoRecompression module initialized successfully on iOS");
        resolve(@{
            @"platform": @"ios",
            @"version": @"0.9.2",
            @"capabilities": @[@"video_analysis", @"smart_compression", @"codec_detection", @"container_rewrap", @"progress_callbacks"]
        });
    });
}

RCT_EXPORT_METHOD(analyzeVideo:(NSString *)filePath
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject)
{
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        @try {
            NSURL *url = [NSURL fileURLWithPath:filePath];
            AVURLAsset *asset = [AVURLAsset URLAssetWithURL:url options:nil];
            
            if (!asset) {
                reject(@"ANALYSIS_ERROR", @"Could not create asset from file path", nil);
                return;
            }
            
            // Get basic properties
            CMTime duration = asset.duration;
            float durationSeconds = CMTimeGetSeconds(duration);
            
            // Get file size
            NSError *error;
            NSDictionary *fileAttributes = [[NSFileManager defaultManager] attributesOfItemAtPath:filePath error:&error];
            long long fileSize = [fileAttributes fileSize];
            
            // Analyze video tracks
            NSArray *videoTracks = [asset tracksWithMediaType:AVMediaTypeVideo];
            NSArray *audioTracks = [asset tracksWithMediaType:AVMediaTypeAudio];
            
            if (videoTracks.count == 0) {
                reject(@"ANALYSIS_ERROR", @"No video tracks found", nil);
                return;
            }
            
            AVAssetTrack *videoTrack = videoTracks.firstObject;
            AVAssetTrack *audioTrack = audioTracks.firstObject;
            
            // Get video properties
            CGSize videoSize = videoTrack.naturalSize;
            float frameRate = videoTrack.nominalFrameRate;
            
            // Get accurate bitrates from track metadata
            long long videoBitrate = 0;
            long long audioBitrate = 0;
            
            // Try to get actual video bitrate from track
            if (videoTrack) {
                // Try estimatedDataRate first (most accurate)
                if (videoTrack.estimatedDataRate > 0) {
                    videoBitrate = (long long)videoTrack.estimatedDataRate;
                } else {
                    // Fallback: calculate from file size if duration available
                    if (durationSeconds > 0) {
                        videoBitrate = (long long)((fileSize * 8.0 / durationSeconds) * 0.85); // Estimate 85% for video
                    }
                }
            }
            
            // Try to get actual audio bitrate from track
            if (audioTrack) {
                // Try estimatedDataRate first (most accurate)
                if (audioTrack.estimatedDataRate > 0) {
                    audioBitrate = (long long)audioTrack.estimatedDataRate;
                } else {
                    // Fallback: use common defaults based on codec
                    audioBitrate = 128000; // 128 kbps default for AAC
                }
            } else {
                audioBitrate = 0; // No audio track
            }
            
            // Get format descriptions to detect codecs
            NSString *videoCodec = @"unknown";
            NSString *audioCodec = @"unknown";
            NSString *container = [filePath.pathExtension lowercaseString];
            
            if (videoTrack.formatDescriptions.count > 0) {
                CMFormatDescriptionRef formatDesc = (__bridge CMFormatDescriptionRef)videoTrack.formatDescriptions.firstObject;
                CMVideoCodecType codecType = CMFormatDescriptionGetMediaSubType(formatDesc);
                
                switch (codecType) {
                    case kCMVideoCodecType_H264:
                        videoCodec = @"h264";
                        break;
                    case kCMVideoCodecType_HEVC:
                        videoCodec = @"hevc";
                        break;
                    case kCMVideoCodecType_VP9:
                        videoCodec = @"vp9";
                        break;
                    case 'vp08': // VP8 codec type
                        videoCodec = @"vp8";
                        break;
                    case 'av01': // AV1 codec type
                        videoCodec = @"av1";
                        break;
                    default:
                        videoCodec = [NSString stringWithFormat:@"unknown_%u", codecType];
                        break;
                }
            }
            
            if (audioTrack && audioTrack.formatDescriptions.count > 0) {
                CMFormatDescriptionRef formatDesc = (__bridge CMFormatDescriptionRef)audioTrack.formatDescriptions.firstObject;
                AudioFormatID codecType = CMFormatDescriptionGetMediaSubType(formatDesc);
                
                switch (codecType) {
                    case kAudioFormatMPEG4AAC:
                        audioCodec = @"aac";
                        break;
                    case kAudioFormatMPEGLayer3:
                        audioCodec = @"mp3";
                        break;
                    case kAudioFormatLinearPCM:
                        audioCodec = @"pcm";
                        break;
                    case kAudioFormatOpus:
                        audioCodec = @"opus";
                        break;
                    case 'vorb': // Vorbis codec type
                        audioCodec = @"vorbis";
                        break;
                    case kAudioFormatFLAC:
                        audioCodec = @"flac";
                        break;
                    default:
                        audioCodec = [NSString stringWithFormat:@"unknown_%u", codecType];
                        break;
                }
            }
            
            resolve(@{
                @"container": container,
                @"videoCodec": videoCodec,
                @"audioCodec": audioCodec,
                @"width": @((int)videoSize.width),
                @"height": @((int)videoSize.height),
                @"duration": @(durationSeconds),
                @"videoBitrate": @(videoBitrate),
                @"audioBitrate": @(audioBitrate),
                @"frameRate": @(frameRate),
                @"fileSize": @(fileSize)
            });
            
        } @catch (NSException *exception) {
            RCTLogError(@"Exception during video analysis: %@", exception);
            reject(@"ANALYSIS_EXCEPTION", @"Exception during video analysis", nil);
        }
    });
}

RCT_EXPORT_METHOD(processVideo:(NSString *)inputPath
                 outputPath:(NSString *)outputPath
                 settings:(NSDictionary *)settings
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject)
{
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        @try {
            NSDate *startTime = [NSDate date];
            
            NSURL *inputURL = [NSURL fileURLWithPath:inputPath];
            NSURL *outputURL = [NSURL fileURLWithPath:outputPath];
            
            RCTLogInfo(@"Processing video - Input: %@", inputPath);
            RCTLogInfo(@"Input URL: %@", inputURL);
            
            // Check if input file exists
            NSFileManager *fileManager = [NSFileManager defaultManager];
            if (![fileManager fileExistsAtPath:inputPath]) {
                RCTLogError(@"Input file does not exist at path: %@", inputPath);
                reject(@"PROCESS_ERROR", @"Input file does not exist", nil);
                return;
            }
            
            // Remove old file if exists
            [[NSFileManager defaultManager] removeItemAtURL:outputURL error:nil];
            
            AVURLAsset *asset = [AVURLAsset URLAssetWithURL:inputURL options:nil];
            
            if (!asset) {
                RCTLogError(@"Could not create AVURLAsset from path: %@", inputPath);
                reject(@"PROCESS_ERROR", @"Could not create asset from input path", nil);
                return;
            }
            
            RCTLogInfo(@"Asset created successfully, proceeding to analysis...");
            
            // Analyze the input first
            [self analyzeAssetAndProcessWithAsset:asset
                                       inputPath:inputPath
                                      outputPath:outputPath
                                        settings:settings
                                       startTime:startTime
                                        resolver:resolve
                                        rejecter:reject];
            
        } @catch (NSException *exception) {
            RCTLogError(@"Exception during video processing: %@", exception);
            reject(@"PROCESS_EXCEPTION", @"Exception during video processing", nil);
        }
    });
}

- (void)analyzeAssetAndProcessWithAsset:(AVURLAsset *)asset
                              inputPath:(NSString *)inputPath
                             outputPath:(NSString *)outputPath
                               settings:(NSDictionary *)settings
                              startTime:(NSDate *)startTime
                               resolver:(RCTPromiseResolveBlock)resolve
                               rejecter:(RCTPromiseRejectBlock)reject
{
    // Get video and audio tracks
    NSArray *videoTracks = [asset tracksWithMediaType:AVMediaTypeVideo];
    NSArray *audioTracks = [asset tracksWithMediaType:AVMediaTypeAudio];
    
    RCTLogInfo(@"Asset analysis: video tracks count: %lu, audio tracks count: %lu", (unsigned long)videoTracks.count, (unsigned long)audioTracks.count);
    RCTLogInfo(@"Asset duration: %f seconds", CMTimeGetSeconds(asset.duration));
    RCTLogInfo(@"Asset playable: %@", asset.playable ? @"YES" : @"NO");
    
    if (videoTracks.count == 0) {
        RCTLogError(@"No video tracks found in asset. Input path: %@", inputPath);
        reject(@"PROCESS_ERROR", @"No video tracks found", nil);
        return;
    }
    
    AVAssetTrack *videoTrack = videoTracks.firstObject;
    AVAssetTrack *audioTrack = audioTracks.firstObject;
    
    // Detect current codecs
    NSString *currentVideoCodec = [self detectVideoCodecFromTrack:videoTrack];
    NSString *currentAudioCodec = [self detectAudioCodecFromTrack:audioTrack];
    NSString *inputContainer = [inputPath.pathExtension lowercaseString];
    
    // Get target settings with defaults
    NSString *targetVideoCodec = settings[@"videoCodec"] ?: @"h264";
    NSString *targetAudioCodec = settings[@"audioCodec"] ?: @"aac";
    NSString *targetContainer = @"mp4";
    
    // Get current bitrates for smart decision making
    long long currentVideoBitrate = 0;
    long long currentAudioBitrate = 0;
    
    if (videoTrack && videoTrack.estimatedDataRate > 0) {
        currentVideoBitrate = (long long)videoTrack.estimatedDataRate;
    }
    if (audioTrack && audioTrack.estimatedDataRate > 0) {
        currentAudioBitrate = (long long)audioTrack.estimatedDataRate;
    }
    
    // Define thresholds for chat optimization (matching Android implementation)
    long long videoRecompressionThreshold = 2000000; // 2 Mbps
    long long audioRecompressionThreshold = 192000;  // 192 kbps
    
    // Check if bitrates are reasonable for chat
    BOOL videoBitrateReasonable = currentVideoBitrate <= videoRecompressionThreshold || currentVideoBitrate == 0;
    BOOL audioBitrateReasonable = currentAudioBitrate <= audioRecompressionThreshold || currentAudioBitrate == 0;
    BOOL codecsMatch = [currentVideoCodec isEqualToString:targetVideoCodec] && [currentAudioCodec isEqualToString:targetAudioCodec];
    BOOL containerMatches = [inputContainer isEqualToString:targetContainer];
    
    RCTLogInfo(@"Smart processing decision factors:");
    RCTLogInfo(@"  Codecs match: %@ (%@==%@, %@==%@)", codecsMatch ? @"YES" : @"NO", currentVideoCodec, targetVideoCodec, currentAudioCodec, targetAudioCodec);
    RCTLogInfo(@"  Container matches: %@ (%@==%@)", containerMatches ? @"YES" : @"NO", inputContainer, targetContainer);
    RCTLogInfo(@"  Video bitrate reasonable: %@ (%lld <= %lld)", videoBitrateReasonable ? @"YES" : @"NO", currentVideoBitrate, videoRecompressionThreshold);
    RCTLogInfo(@"  Audio bitrate reasonable: %@ (%lld <= %lld)", audioBitrateReasonable ? @"YES" : @"NO", currentAudioBitrate, audioRecompressionThreshold);
    
    // Decision logic
    NSString *action = @"recompress"; // Default action
    NSString *exportPreset = AVAssetExportPresetMediumQuality;
    
    // 1. Perfect case: everything matches and bitrates are reasonable
    if (codecsMatch && containerMatches && videoBitrateReasonable && audioBitrateReasonable) {
        
        action = @"passthrough";
        RCTLogInfo(@"Decision: PASSTHROUGH - Already optimal");
        
        // Just copy the file
        NSError *copyError;
        [[NSFileManager defaultManager] copyItemAtPath:inputPath toPath:outputPath error:&copyError];
        
        if (copyError) {
            reject(@"COPY_ERROR", @"Failed to copy file", copyError);
            return;
        }
        
        [self resolveWithResult:action inputPath:inputPath outputPath:outputPath startTime:startTime resolver:resolve rejecter:reject];
        return;
    }
    
    // 2. Good codecs but wrong container or within rewrap threshold
    else if (codecsMatch && videoBitrateReasonable && audioBitrateReasonable) {
        
        action = @"rewrap";
        exportPreset = AVAssetExportPresetPassthrough;
        RCTLogInfo(@"Decision: REWRAP - Correct codecs, change container or minor optimization");
    }
    
    // 3. High bitrates or wrong codecs - need full recompression
    else {
        action = @"recompress";
        RCTLogInfo(@"Decision: RECOMPRESS - Bitrates too high or wrong codecs");
        
        // Choose preset based on settings
        if (settings[@"quality"]) {
            float quality = [settings[@"quality"] floatValue];
            if (quality >= 0.8) {
                exportPreset = AVAssetExportPresetHighestQuality;
            } else if (quality >= 0.6) {
                exportPreset = AVAssetExportPresetMediumQuality;
            } else {
                exportPreset = AVAssetExportPresetLowQuality;
            }
        }
    }
    
    // Perform the export
    AVAssetExportSession *exportSession = [[AVAssetExportSession alloc] initWithAsset:asset presetName:exportPreset];
    
    if (!exportSession) {
        reject(@"EXPORT_SESSION_ERROR", @"Could not create export session", nil);
        return;
    }
    
    exportSession.outputURL = [NSURL fileURLWithPath:outputPath];
    exportSession.outputFileType = AVFileTypeMPEG4;
    exportSession.shouldOptimizeForNetworkUse = [settings[@"optimizeForNetwork"] boolValue] ?: YES;
    
    // Apply custom settings if recompressing
    if ([action isEqualToString:@"recompress"]) {
        [self applyCustomSettingsToExportSession:exportSession settings:settings];
    }
    
    RCTLogInfo(@"Starting video processing with action: %@, preset: %@", action, exportPreset);
    
    [exportSession exportAsynchronouslyWithCompletionHandler:^{
        dispatch_async(dispatch_get_main_queue(), ^{
            switch ([exportSession status]) {
                case AVAssetExportSessionStatusCompleted:
                    RCTLogInfo(@"Video processing completed: %@", outputPath);
                    [self resolveWithResult:action inputPath:inputPath outputPath:outputPath startTime:startTime resolver:resolve rejecter:reject];
                    break;
                case AVAssetExportSessionStatusFailed:
                    RCTLogError(@"Video processing failed: %@", [exportSession error]);
                    reject(@"EXPORT_FAILED", @"Video processing failed", [exportSession error]);
                    break;
                case AVAssetExportSessionStatusCancelled:
                    reject(@"EXPORT_CANCELLED", @"Video processing was cancelled", nil);
                    break;
                default:
                    reject(@"EXPORT_UNKNOWN", @"Unknown export status", nil);
                    break;
            }
        });
    }];
}

- (void)applyCustomSettingsToExportSession:(AVAssetExportSession *)exportSession settings:(NSDictionary *)settings
{
    // This is where you could apply more specific encoding settings
    // However, AVAssetExportSession has limited customization options
    // For more control, you would need to use AVAssetWriter with custom encoding
    
    RCTLogInfo(@"Applying custom settings: %@", settings);
    
    // Note: For full control over video encoding parameters (bitrate, resolution, etc.)
    // we would need to implement AVAssetWriter-based encoding, which is more complex
    // but allows precise control over H.264 parameters, resolution scaling, etc.
}

- (NSString *)detectVideoCodecFromTrack:(AVAssetTrack *)videoTrack
{
    if (!videoTrack || videoTrack.formatDescriptions.count == 0) {
        return @"unknown";
    }
    
    CMFormatDescriptionRef formatDesc = (__bridge CMFormatDescriptionRef)videoTrack.formatDescriptions.firstObject;
    CMVideoCodecType codecType = CMFormatDescriptionGetMediaSubType(formatDesc);
    
    switch (codecType) {
        case kCMVideoCodecType_H264:
            return @"h264";
        case kCMVideoCodecType_HEVC:
            return @"hevc";
        case kCMVideoCodecType_VP9:
            return @"vp9";
        default:
            return @"unknown";
    }
}

- (NSString *)detectAudioCodecFromTrack:(AVAssetTrack *)audioTrack
{
    if (!audioTrack || audioTrack.formatDescriptions.count == 0) {
        return @"unknown";
    }
    
    CMFormatDescriptionRef formatDesc = (__bridge CMFormatDescriptionRef)audioTrack.formatDescriptions.firstObject;
    AudioFormatID codecType = CMFormatDescriptionGetMediaSubType(formatDesc);
    
    switch (codecType) {
        case kAudioFormatMPEG4AAC:
            return @"aac";
        case kAudioFormatMPEGLayer3:
            return @"mp3";
        case kAudioFormatLinearPCM:
            return @"pcm";
        default:
            return @"unknown";
    }
}

- (void)resolveWithResult:(NSString *)action
                inputPath:(NSString *)inputPath
               outputPath:(NSString *)outputPath
                startTime:(NSDate *)startTime
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject
{
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        @try {
            NSTimeInterval processingTime = [[NSDate date] timeIntervalSinceDate:startTime] * 1000; // ms
            
            // Analyze both files for the result
            NSURL *inputURL = [NSURL fileURLWithPath:inputPath];
            NSURL *outputURL = [NSURL fileURLWithPath:outputPath];
            
            // Get original file info
            NSDictionary *originalInfo = [self getQuickFileInfo:inputURL];
            
            // Get final file info
            NSDictionary *finalInfo = [self getQuickFileInfo:outputURL];
            
            resolve(@{
                @"outputPath": outputPath,
                @"action": action,
                @"originalInfo": originalInfo,
                @"finalInfo": finalInfo,
                @"processingTime": @(processingTime)
            });
            
        } @catch (NSException *exception) {
            RCTLogError(@"Exception while preparing result: %@", exception);
            reject(@"RESULT_ERROR", @"Failed to prepare result", nil);
        }
    });
}

- (NSDictionary *)getQuickFileInfo:(NSURL *)url
{
    @try {
        NSError *error;
        NSDictionary *fileAttributes = [[NSFileManager defaultManager] attributesOfItemAtPath:url.path error:&error];
        long long fileSize = fileAttributes ? [fileAttributes fileSize] : 0;
        
        AVURLAsset *asset = [AVURLAsset URLAssetWithURL:url options:nil];
        NSString *container = [url.pathExtension lowercaseString];
        
        if (!asset) {
            return @{
                @"container": container,
                @"videoCodec": @"unknown",
                @"audioCodec": @"unknown",
                @"width": @0,
                @"height": @0,
                @"duration": @0,
                @"videoBitrate": @0,
                @"audioBitrate": @0,
                @"frameRate": @0,
                @"fileSize": @(fileSize)
            };
        }
        
        NSArray *videoTracks = [asset tracksWithMediaType:AVMediaTypeVideo];
        NSArray *audioTracks = [asset tracksWithMediaType:AVMediaTypeAudio];
        
        AVAssetTrack *videoTrack = videoTracks.firstObject;
        AVAssetTrack *audioTrack = audioTracks.firstObject;
        
        CGSize videoSize = videoTrack ? videoTrack.naturalSize : CGSizeZero;
        float frameRate = videoTrack ? videoTrack.nominalFrameRate : 0;
        float duration = CMTimeGetSeconds(asset.duration);
        
        return @{
            @"container": container,
            @"videoCodec": [self detectVideoCodecFromTrack:videoTrack],
            @"audioCodec": [self detectAudioCodecFromTrack:audioTrack],
            @"width": @((int)videoSize.width),
            @"height": @((int)videoSize.height),
            @"duration": @(duration),
            @"videoBitrate": @0, // Quick analysis doesn't calculate accurate bitrate
            @"audioBitrate": @0,
            @"frameRate": @(frameRate),
            @"fileSize": @(fileSize)
        };
    } @catch (NSException *exception) {
        return @{};
    }
}

@end
