#!/bin/bash

# Pre-Publish Validation Script
# Comprehensive checks before publishing to npm

set -e

echo "🚀 Running PRE-PUBLISH validation..."
echo "=================================="

# 1. Clean and rebuild
echo "1️⃣  Clean build..."
npm run clean
npm run build

# 2. Run all tests
echo "2️⃣  Running tests..."
npm test

# 3. Code quality checks
echo "3️⃣  Code quality..."
npm run lint

# 4. TypeScript validation
echo "4️⃣  TypeScript validation..."
npm run typecheck

# 5. Check package structure
echo "5️⃣  Package structure..."
echo "✅ Checking required files..."
for file in "lib/commonjs/index.js" "lib/module/index.js" "lib/typescript/index.d.ts" "package.json" "README.md"; do
    if [ -f "$file" ]; then
        echo "   ✓ $file"
    else
        echo "   ❌ $file MISSING"
        exit 1
    fi
done

# 6. Check native implementations
echo "6️⃣  Native implementations..."
if [ -f "ios/VideoRecompression.mm" ]; then
    echo "   ✓ iOS implementation"
else
    echo "   ❌ iOS implementation MISSING"
    exit 1
fi

if [ -f "android/src/main/java/com/videorecompression/VideoRecompressionModule.kt" ]; then
    echo "   ✓ Android implementation"
else
    echo "   ❌ Android implementation MISSING"
    exit 1
fi

# 7. Check git status
echo "7️⃣  Git status..."
if [ -n "$(git status --porcelain)" ]; then
    echo "⚠️  Uncommitted changes detected:"
    git status --short
    echo ""
    echo "Consider committing changes before publishing."
else
    echo "   ✓ No uncommitted changes"
fi

# 8. Check version
echo "8️⃣  Version check..."
CURRENT_VERSION=$(node -p "require('./package.json').version")
echo "   Current version: $CURRENT_VERSION"

# 9. Simulate npm pack
echo "9️⃣  Package simulation..."
npm pack --dry-run > /dev/null
echo "   ✓ Package simulation successful"

# 10. Final summary
echo ""
echo "🎯 PRE-PUBLISH SUMMARY"
echo "====================="
echo "✅ Build: SUCCESSFUL"
echo "✅ Tests: PASSING"
echo "✅ Code Quality: PASSING"
echo "✅ TypeScript: VALID"
echo "✅ Package Structure: COMPLETE"
echo "✅ Native Code: PRESENT"
echo "✅ Package Simulation: SUCCESSFUL"
echo ""
echo "🚀 READY FOR PUBLISH!"
echo ""
echo "To publish:"
echo "1. npm version [patch|minor|major]"
echo "2. git push --tags"
echo "3. npm publish"
echo ""
