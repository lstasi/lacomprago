# GitHub Configuration

This directory contains the CI/CD configuration for the LaCompraGo project.

## Workflows

### android-build.yml
Automated build workflow that runs on every push and pull request to main and develop branches.

**Jobs:**
- **Build**: Compiles both debug and release APKs and uploads them as artifacts
- **Lint**: Runs Android lint checks and uploads the report
- **Test**: Runs unit tests and uploads test results

**Artifacts:**
- Debug APK (retained for 30 days)
- Release APK (retained for 90 days)
- Lint reports (retained for 30 days)
- Test results (retained for 30 days)

### release.yml
Release workflow for creating GitHub releases with APK files.

**Triggers:**
- Push tags matching `v*.*.*` (e.g., v1.0.0)
- Push tags matching `phase-*` (e.g., phase-3, phase-4)
- Manual workflow dispatch with custom version tag

**Outputs:**
- GitHub Release with attached APK file
- APK named as `LaCompraGo-{version}.apk`

**Usage:**
```bash
# Create a phase release
git tag phase-3 -m "Phase 3: Token Authentication Complete"
git push origin phase-3

# Create a version release
git tag v1.0.0 -m "Version 1.0.0"
git push origin v1.0.0

# Or use GitHub Actions UI to manually trigger release
```

## Dependabot

The `dependabot.yml` configuration automatically:
- Monitors GitHub Actions versions weekly
- Monitors Gradle dependencies weekly
- Creates pull requests for dependency updates

**Android 34 Version Pinning:**
Dependabot is configured to maintain compatibility with Android 14 (SDK 34) by ignoring major version updates for:
- Android Gradle Plugin
- AndroidX Core KTX
- AndroidX Lifecycle libraries
- AppCompat
- Material Design Components

This ensures the app remains compatible with its minimum and target SDK version of Android 34.

## APK Installation

APKs built by the workflows can be downloaded from:
1. **Actions tab** → Select workflow run → Scroll to Artifacts section
2. **Releases page** → Download APK from any release

To install on your Android 14+ device:
1. Enable "Install from unknown sources" for your browser/file manager
2. Download the APK file
3. Open the APK file to install
4. Grant necessary permissions when prompted
