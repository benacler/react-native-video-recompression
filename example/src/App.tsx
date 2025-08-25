import React, { useState, useEffect } from 'react';
import {
  StyleSheet,
  View,
  Text,
  TouchableOpacity,
  ScrollView,
  Alert,
  ActivityIndicator,
  Platform,
} from 'react-native';
import DocumentPicker from 'react-native-document-picker';
import RNFS from 'react-native-fs';
import VideoRecompression, { 
  VideoInfo, 
  CompressionResult,
  CompressionSettings 
} from 'react-native-video-recompression';

interface TestResult {
  success: boolean;
  message: string;
  data?: any;
  error?: string;
}

export default function App() {
  const [isLoading, setIsLoading] = useState(false);
  const [moduleInfo, setModuleInfo] = useState<any>(null);
  const [selectedFile, setSelectedFile] = useState<string | null>(null);
  const [videoInfo, setVideoInfo] = useState<VideoInfo | null>(null);
  const [testResults, setTestResults] = useState<TestResult[]>([]);
  const [processingProgress, setProcessingProgress] = useState<number>(0);

  useEffect(() => {
    testInitialization();
  }, []);

  const testInitialization = async () => {
    setIsLoading(true);
    try {
      const info = await VideoRecompression.init();
      setModuleInfo(info);
      addTestResult(true, 'Module initialized successfully', info);
    } catch (error) {
      addTestResult(false, 'Module initialization failed', null, error.message);
    }
    setIsLoading(false);
  };

  const addTestResult = (success: boolean, message: string, data?: any, error?: string) => {
    const result: TestResult = { success, message, data, error };
    setTestResults(prev => [result, ...prev]);
  };

  const selectVideoFile = async () => {
    try {
      const res = await DocumentPicker.pick({
        type: [DocumentPicker.types.video],
      });
      
      if (res && res[0]) {
        const filePath = res[0].uri;
        setSelectedFile(filePath);
        addTestResult(true, `Video file selected: ${res[0].name}`, { 
          name: res[0].name,
          size: res[0].size,
          type: res[0].type,
          uri: filePath
        });
      }
    } catch (err) {
      if (!DocumentPicker.isCancel(err)) {
        addTestResult(false, 'File selection failed', null, err.message);
      }
    }
  };

  const analyzeVideo = async () => {
    if (!selectedFile) {
      Alert.alert('Error', 'Please select a video file first');
      return;
    }

    setIsLoading(true);
    try {
      const info = await VideoRecompression.analyzeVideo(selectedFile);
      setVideoInfo(info);
      addTestResult(true, 'Video analysis completed', info);
    } catch (error) {
      addTestResult(false, 'Video analysis failed', null, error.message);
    }
    setIsLoading(false);
  };

  const processVideo = async (settings?: CompressionSettings) => {
    if (!selectedFile) {
      Alert.alert('Error', 'Please select a video file first');
      return;
    }

    setIsLoading(true);
    setProcessingProgress(0);

    const outputDir = Platform.OS === 'ios' 
      ? RNFS.DocumentDirectoryPath 
      : RNFS.ExternalDirectoryPath;
    
    const timestamp = Date.now();
    const outputPath = `${outputDir}/processed_video_${timestamp}.mp4`;

    try {
      const result = await VideoRecompression.processVideo(
        selectedFile,
        outputPath,
        settings,
        (progress) => {
          setProcessingProgress(progress);
        }
      );
      
      addTestResult(true, `Video processing completed (${result.action})`, {
        ...result,
        outputSize: await getFileSize(result.outputPath)
      });
      
    } catch (error) {
      addTestResult(false, 'Video processing failed', null, error.message);
    }
    
    setIsLoading(false);
    setProcessingProgress(0);
  };

  const getFileSize = async (filePath: string): Promise<number> => {
    try {
      const stat = await RNFS.stat(filePath);
      return stat.size;
    } catch {
      return 0;
    }
  };

  const runAllTests = async () => {
    if (!selectedFile) {
      Alert.alert('Error', 'Please select a video file first');
      return;
    }

    // Clear previous results
    setTestResults([]);
    
    // Test 1: Module initialization (already done)
    
    // Test 2: Video analysis
    await analyzeVideo();
    
    // Test 3: Process with default settings
    await new Promise(resolve => setTimeout(resolve, 1000)); // Brief pause
    await processVideo();
    
    // Test 4: Process with custom settings
    await new Promise(resolve => setTimeout(resolve, 1000));
    await processVideo({
      quality: 0.8,
      maxWidth: 1280,
      videoCodec: 'h264',
      optimizeForNetwork: true
    });
    
    addTestResult(true, 'All tests completed', null);
  };

  const clearResults = () => {
    setTestResults([]);
    setVideoInfo(null);
    setSelectedFile(null);
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Video Recompression Test App</Text>
      
      {moduleInfo && (
        <View style={styles.moduleInfo}>
          <Text style={styles.moduleText}>
            Platform: {moduleInfo.platform} | Version: {moduleInfo.version}
          </Text>
          <Text style={styles.moduleText}>
            Capabilities: {moduleInfo.capabilities?.join(', ') || 'None'}
          </Text>
        </View>
      )}

      {isLoading && (
        <View style={styles.loadingContainer}>
          <ActivityIndicator size="large" color="#007AFF" />
          {processingProgress > 0 && (
            <Text style={styles.progressText}>
              Progress: {Math.round(processingProgress * 100)}%
            </Text>
          )}
        </View>
      )}

      <View style={styles.buttonContainer}>
        <TouchableOpacity style={styles.button} onPress={selectVideoFile}>
          <Text style={styles.buttonText}>Select Video File</Text>
        </TouchableOpacity>
        
        <TouchableOpacity 
          style={[styles.button, !selectedFile && styles.buttonDisabled]} 
          onPress={analyzeVideo}
          disabled={!selectedFile}
        >
          <Text style={styles.buttonText}>Analyze Video</Text>
        </TouchableOpacity>
        
        <TouchableOpacity 
          style={[styles.button, !selectedFile && styles.buttonDisabled]} 
          onPress={() => processVideo()}
          disabled={!selectedFile}
        >
          <Text style={styles.buttonText}>Process (Default)</Text>
        </TouchableOpacity>
        
        <TouchableOpacity 
          style={[styles.button, styles.primaryButton, !selectedFile && styles.buttonDisabled]} 
          onPress={runAllTests}
          disabled={!selectedFile}
        >
          <Text style={styles.buttonText}>Run All Tests</Text>
        </TouchableOpacity>
        
        <TouchableOpacity style={[styles.button, styles.secondaryButton]} onPress={clearResults}>
          <Text style={styles.buttonText}>Clear Results</Text>
        </TouchableOpacity>
      </View>

      <ScrollView style={styles.resultsContainer}>
        <Text style={styles.resultsTitle}>Test Results:</Text>
        {testResults.map((result, index) => (
          <View 
            key={index} 
            style={[
              styles.resultItem, 
              result.success ? styles.resultSuccess : styles.resultError
            ]}
          >
            <Text style={styles.resultMessage}>{result.message}</Text>
            {result.data && (
              <Text style={styles.resultData}>
                {JSON.stringify(result.data, null, 2)}
              </Text>
            )}
            {result.error && (
              <Text style={styles.resultError}>{result.error}</Text>
            )}
          </View>
        ))}
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
    paddingTop: 50,
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    textAlign: 'center',
    marginBottom: 20,
    color: '#333',
  },
  moduleInfo: {
    backgroundColor: '#e7f3ff',
    padding: 10,
    marginHorizontal: 20,
    marginBottom: 20,
    borderRadius: 8,
  },
  moduleText: {
    fontSize: 12,
    color: '#666',
  },
  loadingContainer: {
    alignItems: 'center',
    marginVertical: 20,
  },
  progressText: {
    marginTop: 10,
    color: '#007AFF',
    fontWeight: '600',
  },
  buttonContainer: {
    paddingHorizontal: 20,
    marginBottom: 20,
  },
  button: {
    backgroundColor: '#007AFF',
    padding: 15,
    borderRadius: 8,
    marginBottom: 10,
    alignItems: 'center',
  },
  buttonDisabled: {
    backgroundColor: '#ccc',
  },
  primaryButton: {
    backgroundColor: '#34C759',
  },
  secondaryButton: {
    backgroundColor: '#FF9500',
  },
  buttonText: {
    color: 'white',
    fontSize: 16,
    fontWeight: '600',
  },
  resultsContainer: {
    flex: 1,
    marginHorizontal: 20,
  },
  resultsTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    marginBottom: 10,
    color: '#333',
  },
  resultItem: {
    padding: 10,
    borderRadius: 8,
    marginBottom: 10,
  },
  resultSuccess: {
    backgroundColor: '#d4edda',
    borderColor: '#c3e6cb',
    borderWidth: 1,
  },
  resultError: {
    backgroundColor: '#f8d7da',
    borderColor: '#f5c6cb',
    borderWidth: 1,
  },
  resultMessage: {
    fontWeight: '600',
    color: '#333',
  },
  resultData: {
    marginTop: 5,
    fontSize: 12,
    color: '#666',
    fontFamily: Platform.OS === 'ios' ? 'Courier' : 'monospace',
  },
  resultErrorText: {
    marginTop: 5,
    color: '#dc3545',
    fontSize: 12,
  },
});
