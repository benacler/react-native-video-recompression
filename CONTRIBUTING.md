# Contributing to React Native Video Recompression

Thank you for considering contributing to react-native-video-recompression! We welcome contributions from the community.

## Code of Conduct

This project and everyone participating in it is governed by our [Code of Conduct](CODE_OF_CONDUCT.md). By participating, you are expected to uphold this code.

## How Can I Contribute?

### Reporting Bugs

Before creating bug reports, please check the existing issues as you might find out that the bug has already been reported. When you are creating a bug report, please include as many details as possible:

- Use a clear and descriptive title
- Describe the exact steps which reproduce the problem
- Provide specific examples to demonstrate the steps
- Describe the behavior you observed after following the steps
- Explain which behavior you expected to see instead and why
- Include screenshots if applicable
- Include your environment details (React Native version, iOS/Android version, device info)

### Suggesting Enhancements

Enhancement suggestions are tracked as GitHub issues. When creating an enhancement suggestion, please include:

- Use a clear and descriptive title
- Provide a step-by-step description of the suggested enhancement
- Provide specific examples to demonstrate the steps
- Describe the current behavior and explain which behavior you expected to see instead
- Explain why this enhancement would be useful

### Pull Requests

1. Fork the repository
2. Create a new branch from `main` for your feature or bug fix
3. Make your changes
4. Add tests for your changes
5. Ensure all tests pass
6. Update documentation if necessary
7. Submit a pull request

#### Pull Request Guidelines

- Follow the existing code style
- Write clear, concise commit messages
- Include tests for new functionality
- Update documentation as needed
- Ensure CI passes
- Keep PRs focused and atomic

## Development Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/YOUR_USERNAME/react-native-video-recompression.git
   cd react-native-video-recompression
   ```

2. Install dependencies:
   ```bash
   yarn install
   ```

3. Run tests:
   ```bash
   yarn test
   ```

4. Run linting:
   ```bash
   yarn lint
   ```

5. Run type checking:
   ```bash
   yarn typecheck
   ```

6. Build the project:
   ```bash
   yarn build
   ```

## Project Structure

```
├── android/             # Android native implementation
├── ios/                 # iOS native implementation  
├── src/                 # TypeScript source code
├── lib/                 # Built library output
├── scripts/             # Utility scripts
├── docs/                # Documentation
└── __tests__/           # Test files
```

## Native Development

### iOS Development

- Requires Xcode 12+
- iOS deployment target: 11.0+
- Written in Objective-C++
- Uses AVFoundation framework

### Android Development

- Requires Android Studio
- Minimum SDK: API 21 (Android 5.0)
- Target SDK: API 33
- Written in Kotlin
- Uses MediaMuxer/MediaExtractor APIs

## Testing

- Unit tests: Jest
- Native testing: Platform-specific test frameworks
- Integration testing: Manual with real React Native apps

Run tests with:
```bash
yarn test
yarn test:android
yarn test:ios
```

## Documentation

When adding new features or changing existing functionality:

1. Update the README.md
2. Update API documentation in code comments
3. Add usage examples to documentation
4. Update CHANGELOG.md

## Release Process

This project uses semantic-release for automated releases:

1. Make changes on feature branches
2. Create pull requests to `main`
3. Merging to `main` triggers automated release
4. Version numbers follow semantic versioning

## Code Style

- Use TypeScript for all source code
- Follow ESLint configuration
- Use Prettier for formatting
- Write meaningful commit messages following conventional commits

## Need Help?

- Check existing [issues](https://github.com/YOUR_USERNAME/react-native-video-recompression/issues)
- Create a new issue with detailed information
- Join discussions in existing issues

Thank you for contributing! 🎉
