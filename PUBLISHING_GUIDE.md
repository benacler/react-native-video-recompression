# React Native Video Recompression - Publishing Guide

This document outlines the steps needed to scaffold and publish the `react-native-video-recompression` package as a standalone GitHub repository and npm package.

## Prerequisites

- Node.js (v16 or higher)
- npm or yarn
- Git
- GitHub account
- npm account (for publishing)

## 1. Repository Setup

### Create New Repository
1. Create a new empty GitHub repository named `react-native-video-recompression`
2. Clone the repository locally:
   ```bash
   git clone https://github.com/YOUR_USERNAME/react-native-video-recompression.git
   cd react-native-video-recompression
   ```

### Copy Package Files
Copy the entire contents of this package directory to the new repository:
- `src/` - TypeScript source files
- `android/` - Android native implementation
- `ios/` - iOS native implementation
- `package.json` - Package configuration
- `react-native-video-recompression.podspec` - iOS CocoaPods spec
- `README.md` - Documentation
- `.gitignore` - Git ignore rules

## 2. Package Configuration Updates

### Update package.json
Update the following fields in `package.json`:

```json
{
  "name": "react-native-video-recompression",
  "version": "1.0.0",
  "description": "A React Native library for intelligent video processing with native performance. Supports video analysis, quality-preserving format conversion, and smart recompression with customizable settings",
  "main": "lib/commonjs/index.js",
  "module": "lib/module/index.js",
  "types": "lib/typescript/src/index.d.ts",
  "react-native": "src/index.ts",
  "source": "src/index.ts",
  "files": [
    "src",
    "lib",
    "android",
    "ios",
    "react-native-video-recompression.podspec",
    "!android/build",
    "!android/.cxx",
    "!android/local.properties",
    "!**/__tests__",
    "!**/__fixtures__",
    "!**/__mocks__"
  ],
  "scripts": {
    "test": "jest",
    "typescript": "tsc --noEmit",
    "lint": "eslint \"**/*.{js,ts,tsx}\"",
    "prepare": "bob build",
    "release": "release-it",
    "example": "npm --prefix example",
    "pods": "cd example && pod-install --quiet",
    "bootstrap": "npm run example i && npm run pods"
  },
  "keywords": [
    "react-native",
    "video",
    "compression",
    "video-processing",
    "video-analysis", 
    "format-conversion",
    "ios",
    "android",
    "native",
    "recompression",
    "codec",
    "mobile"
  ],
  "repository": {
    "type": "git",
    "url": "git+https://github.com/YOUR_USERNAME/react-native-video-recompression.git"
  },
  "author": "Your Name <your.email@example.com>",
  "license": "MIT",
  "bugs": {
    "url": "https://github.com/YOUR_USERNAME/react-native-video-recompression/issues"
  },
  "homepage": "https://github.com/YOUR_USERNAME/react-native-video-recompression#readme",
  "publishConfig": {
    "registry": "https://registry.npmjs.org/"
  },
  "devDependencies": {
    "@commitlint/config-conventional": "^17.0.2",
    "@react-native-community/eslint-config": "^3.0.2",
    "@release-it/conventional-changelog": "^5.0.0",
    "@types/jest": "^28.1.2",
    "@types/react": "~17.0.21",
    "@types/react-native": "0.68.0",
    "commitlint": "^17.0.2",
    "eslint": "^8.4.1",
    "eslint-config-prettier": "^8.5.0",
    "eslint-plugin-prettier": "^4.0.0",
    "husky": "^8.0.1",
    "jest": "^28.1.1",
    "pod-install": "^0.1.0",
    "prettier": "^2.0.5",
    "react": "17.0.2",
    "react-native": "0.68.2",
    "react-native-builder-bob": "^0.18.3",
    "release-it": "^15.0.0",
    "typescript": "^4.5.2"
  },
  "peerDependencies": {
    "react": "*",
    "react-native": "*"
  },
  "jest": {
    "preset": "react-native",
    "modulePathIgnorePatterns": [
      "<rootDir>/example/node_modules",
      "<rootDir>/lib/"
    ]
  },
  "commitlint": {
    "extends": ["@commitlint/config-conventional"]
  },
  "release-it": {
    "git": {
      "commitMessage": "chore: release ${version}",
      "tagName": "v${version}"
    },
    "npm": {
      "publish": true
    },
    "github": {
      "release": true
    },
    "plugins": {
      "@release-it/conventional-changelog": {
        "preset": "angular"
      }
    }
  },
  "eslintConfig": {
    "root": true,
    "extends": ["@react-native-community", "prettier"],
    "rules": {
      "prettier/prettier": [
        "error",
        {
          "quoteProps": "consistent",
          "singleQuote": true,
          "tabWidth": 2,
          "trailingComma": "es5",
          "useTabs": false
        }
      ]
    }
  },
  "eslintIgnore": [
    "node_modules/",
    "lib/"
  ],
  "prettier": {
    "quoteProps": "consistent",
    "singleQuote": true,
    "tabWidth": 2,
    "trailingComma": "es5",
    "useTabs": false
  },
  "react-native-builder-bob": {
    "source": "src",
    "output": "lib",
    "targets": [
      "commonjs",
      "module",
      [
        "typescript",
        {
          "project": "tsconfig.build.json"
        }
      ]
    ]
  }
}
```

## 3. Essential Files to Add

### TypeScript Configuration

Create `tsconfig.json`:
```json
{
  "compilerOptions": {
    "baseUrl": "./",
    "paths": {
      "react-native-video-recompression": ["./src/index"]
    },
    "allowUnreachableCode": false,
    "allowUnusedLabels": false,
    "esModuleInterop": true,
    "forceConsistentCasingInFileNames": true,
    "jsx": "react",
    "lib": ["esnext"],
    "module": "esnext",
    "moduleResolution": "node",
    "noFallthroughCasesInSwitch": true,
    "noImplicitReturns": true,
    "noImplicitUseStrict": false,
    "noStrictGenericChecks": false,
    "noUnusedLocals": true,
    "noUnusedParameters": true,
    "resolveJsonModule": true,
    "skipLibCheck": true,
    "strict": true,
    "target": "esnext"
  },
  "exclude": [
    "node_modules",
    "lib",
    "example"
  ]
}
```

Create `tsconfig.build.json`:
```json
{
  "extends": "./tsconfig",
  "exclude": [
    "example"
  ]
}
```

### Git Configuration

Create `.gitignore`:
```
# Dependencies
node_modules/
npm-debug.log*
yarn-debug.log*
yarn-error.log*

# Runtime data
pids
*.pid
*.seed
*.pid.lock

# Build outputs
lib/
*.tgz

# OS generated files
.DS_Store
.DS_Store?
._*
.Spotlight-V100
.Trashes
ehthumbs.db
Thumbs.db

# IDE files
.vscode/
.idea/
*.swp
*.swo

# Logs
logs
*.log

# Coverage directory used by tools like istanbul
coverage/

# Android
android/build/
android/.gradle
android/local.properties
android/.cxx/
android/app/build/

# iOS
ios/build/
ios/Pods/
ios/*.xcworkspace

# Example app
example/node_modules/
example/ios/Pods/
example/ios/build/
example/android/build/
example/android/.gradle
example/android/local.properties
```

### Commitlint Configuration

Create `.commitlintrc.js`:
```javascript
module.exports = {extends: ['@commitlint/config-conventional']};
```

### Husky Configuration

Create `.husky/commit-msg`:
```bash
#!/bin/sh
. "$(dirname "$0")/_/husky.sh"

npx --no-install commitlint --edit "$1"
```

Create `.husky/pre-commit`:
```bash
#!/bin/sh
. "$(dirname "$0")/_/husky.sh"

npm run lint && npm run typescript
```

## 4. Example App Setup

### Create Example Directory Structure
```
example/
├── App.tsx
├── index.js
├── metro.config.js
├── package.json
├── tsconfig.json
├── android/
│   ├── app/
│   ├── build.gradle
│   ├── gradle.properties
│   ├── gradlew
│   ├── gradlew.bat
│   └── settings.gradle
└── ios/
    ├── Example/
    ├── Example.xcodeproj/
    └── Podfile
```

### Example App package.json
```json
{
  "name": "react-native-video-recompression-example",
  "version": "0.0.1",
  "private": true,
  "scripts": {
    "android": "react-native run-android",
    "ios": "react-native run-ios",
    "start": "react-native start",
    "pods": "pod-install"
  },
  "dependencies": {
    "react": "17.0.2",
    "react-native": "0.68.2",
    "react-native-video-recompression": "link:../"
  },
  "devDependencies": {
    "pod-install": "^0.1.0"
  }
}
```

## 5. Documentation Updates

### Update README.md
Enhance the README with:
- Clear installation instructions
- Comprehensive API documentation
- Usage examples
- Platform-specific setup
- Troubleshooting guide
- Contributing guidelines

### Add CHANGELOG.md
```markdown
# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.0.0] - 2024-01-XX
### Added
- Initial release
- Smart video processing with automatic strategy selection
- Video analysis and metadata extraction
- Quality-preserving format conversion
- Custom compression settings
- iOS and Android support
```

### Add CONTRIBUTING.md
```markdown
# Contributing

We welcome contributions to react-native-video-recompression!

## Development Setup

1. Clone the repository
2. Install dependencies: `npm install`
3. Run the example: `npm run example ios` or `npm run example android`

## Submitting Changes

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Ensure all tests pass
6. Submit a pull request

## Code Style

We use ESLint and Prettier for code formatting. Run `npm run lint` to check your code.
```

## 6. Pre-Publication Steps

### Install Dependencies
```bash
npm install
```

### Run Quality Checks
```bash
npm run lint          # Check code style
npm run typescript    # Check TypeScript
npm test             # Run tests
npm run build        # Build the package
```

### Test Installation
```bash
npm pack              # Create tarball
cd example
npm install ../react-native-video-recompression-1.0.0.tgz
```

## 7. Publishing Process

### Initial Release
```bash
# Login to npm
npm login

# Publish to npm
npm publish

# Create GitHub release
git tag v1.0.0
git push origin v1.0.0
```

### Automated Releases
Use `release-it` for automated releases:
```bash
npm run release
```

## 8. Post-Publication

### Documentation
- Update GitHub repository description
- Add topics/tags on GitHub
- Create comprehensive wiki if needed

### Community
- Submit to React Native directory
- Share on relevant communities
- Monitor issues and PRs

## 9. Maintenance

### Regular Tasks
- Keep dependencies updated
- Monitor for security vulnerabilities
- Respond to issues and PRs
- Test with new React Native versions
- Update documentation

### Version Management
Follow semantic versioning:
- PATCH: Bug fixes
- MINOR: New features (backward compatible)
- MAJOR: Breaking changes

---

This guide provides a complete roadmap for transforming your local package into a professional, publishable React Native library. Follow each step carefully and customize the configurations to match your specific requirements.
