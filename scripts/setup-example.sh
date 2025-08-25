#!/bin/bash

# Setup Example App Script  
# Creates basic iOS and Android project structure for testing the video recompression library

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(dirname "$SCRIPT_DIR")"
EXAMPLE_DIR="$ROOT_DIR/example"

echo "🚀 Setting up simple React Native example app structure..."

# Check if we're in the right directory
if [ ! -f "$ROOT_DIR/package.json" ]; then
    echo "❌ Error: Must be run from the react-native-video-recompression root directory"
    exit 1
fi

cd "$EXAMPLE_DIR"

echo "📱 Creating iOS project structure..."
if [ ! -d "ios" ]; then
    mkdir -p ios
    
    # Create basic iOS project files
    cat > ios/Podfile << 'EOF'
require_relative '../node_modules/react-native/scripts/react_native_pods'
require_relative '../node_modules/@react-native-community/cli-platform-ios/native_modules'

platform :ios, '11.0'

target 'VideoRecompressionExample' do
  config = use_native_modules!

  use_react_native!(
    :path => '../node_modules/react-native',
    :hermes_enabled => true
  )

  pod 'react-native-video-recompression', :path => '../..'

  target 'VideoRecompressionExampleTests' do
    inherit! :complete
  end

  post_install do |installer|
    react_native_post_install(installer)
  end
end
EOF

    echo "✅ Created iOS Podfile"
fi

echo "🤖 Creating Android project structure..."
if [ ! -d "android" ]; then
    mkdir -p android/app/src/main/java/com/videorecompressionexample
    
    # Create basic Android files
    cat > android/settings.gradle << 'EOF'
rootProject.name = 'VideoRecompressionExample'
include ':app'
include ':react-native-video-recompression'
project(':react-native-video-recompression').projectDir = new File(rootProject.projectDir, '../android')
EOF

    cat > android/build.gradle << 'EOF'
buildscript {
    ext {
        buildToolsVersion = "33.0.0"
        minSdkVersion = 21
        compileSdkVersion = 33
        targetSdkVersion = 33
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.0.1")
        classpath("com.facebook.react:react-native-gradle-plugin")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url("https://www.jitpack.io") }
    }
}
EOF

    cat > android/app/build.gradle << 'EOF'
apply plugin: "com.android.application"
apply plugin: "com.facebook.react"

android {
    compileSdkVersion rootProject.ext.compileSdkVersion
    
    defaultConfig {
        applicationId "com.videorecompressionexample"
        minSdkVersion rootProject.ext.minSdkVersion
        targetSdkVersion rootProject.ext.targetSdkVersion
        versionCode 1
        versionName "1.0"
    }
    
    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro"
        }
    }
}

dependencies {
    implementation "com.facebook.react:react-native:+"
    implementation project(':react-native-video-recompression')
}
EOF

    echo "✅ Created Android project files"
fi

echo "📦 Updating package.json..."
# Ensure package.json has the right scripts
if [ -f "package.json" ]; then
    # Update package.json to have proper scripts
    node -e "
    const fs = require('fs');
    const pkg = JSON.parse(fs.readFileSync('package.json', 'utf8'));
    
    // Add proper scripts
    pkg.scripts = {
        'android': 'react-native run-android',
        'ios': 'react-native run-ios',
        'start': 'react-native start',
        'test': 'echo \"No tests specified\"'
    };
    
    // Ensure dependencies
    pkg.dependencies = {
        ...pkg.dependencies,
        'react': '18.2.0',
        'react-native': '0.72.7',
        'react-native-video-recompression': 'file:..',
        'react-native-document-picker': '^9.1.1',
        'react-native-fs': '^2.20.0'
    };
    
    fs.writeFileSync('package.json', JSON.stringify(pkg, null, 2));
    "
fi

echo "🎯 Installation instructions:"
echo ""
echo "To test the library:"
echo ""
echo "1. Complete setup:"
echo "   cd example"
echo "   npm install"
echo ""
echo "2. For iOS testing:"
echo "   cd ios && pod install && cd .."
echo "   npm run ios"
echo ""
echo "3. For Android testing:"
echo "   npm run android"
echo ""
echo "📝 Note: This creates a minimal structure."
echo "   For full React Native features, consider using:"
echo "   npx react-native init TestApp"
echo ""
echo "✅ Basic example app structure created!"
