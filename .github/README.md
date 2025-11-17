# GitHub Copilot Configuration

This directory contains GitHub Copilot instructions and configurations for the LaCompraGo project.

## 📁 Structure

```
.github/
├── copilot-instructions.md           # Main repository-level instructions
└── instructions/
    ├── kotlin-android.instructions.md # Kotlin & Android-specific guidance
    └── testing.instructions.md        # Testing patterns and guidelines
```

## 📖 Files Overview

### `copilot-instructions.md`
The main instruction file that applies to the entire repository. It includes:
- Project overview and architecture (Simplified MVVM)
- Minimal dependencies philosophy
- General coding guidelines
- Data management patterns
- UI guidelines
- Security considerations
- Build and development information

### `instructions/kotlin-android.instructions.md`
Language and platform-specific instructions that apply to all Kotlin files (`**/*.kt`):
- Kotlin idioms and language features
- Android patterns (ViewModel, Repository, Activity, etc.)
- Coroutines usage with proper dispatchers
- OkHttp networking patterns
- EncryptedSharedPreferences for secure storage
- Common anti-patterns to avoid
- Code examples showing good vs. bad practices

### `instructions/testing.instructions.md`
Testing-specific instructions that apply to test files (`**/test/**/*.kt` and `**/androidTest/**/*.kt`):
- Unit testing patterns for ViewModels and Repositories
- Coroutines testing with test dispatchers
- Instrumented testing with Espresso
- Integration testing
- AAA (Arrange-Act-Assert) pattern
- Edge case testing
- Mocking guidelines

## 🎯 Purpose

These instructions help GitHub Copilot provide:
- **Contextual code suggestions** aligned with the project's architecture
- **Consistent coding patterns** following Kotlin and Android best practices
- **Appropriate dependencies** respecting the minimal dependencies philosophy
- **Proper testing approaches** for different types of tests
- **Security-conscious code** following Android security guidelines

## 🚀 How It Works

GitHub Copilot automatically reads these instruction files when:
- You're working in VS Code with the GitHub Copilot extension
- You assign issues to `@copilot` in GitHub
- You interact with Copilot in the GitHub interface

The instructions provide context that helps Copilot understand:
- What this project is about
- What architecture patterns to follow
- What libraries and frameworks we use (and don't use)
- What coding standards to apply
- How to structure tests

## 📝 Updating Instructions

When updating these files:
1. Keep instructions clear and specific
2. Include code examples where helpful
3. Update the YAML frontmatter for file-specific instructions
4. Test with Copilot to ensure it understands the guidance
5. Update this README if you add new instruction files

## 🔗 References

- [GitHub Copilot Documentation](https://docs.github.com/en/copilot)
- [VS Code Copilot Customization](https://code.visualstudio.com/docs/copilot/copilot-customization)
- [Best Practices for Copilot Coding Agent](https://docs.github.com/en/copilot/tutorials/coding-agent/get-the-best-results)
- [Awesome Copilot Agents Repository](https://github.com/Code-and-Sorts/awesome-copilot-agents)

## 💡 Tips

- Reference these instructions when creating issues for `@copilot`
- Mention specific sections when you need focused guidance
- Keep instructions updated as the project evolves
- Add new instruction files for specialized domains as needed

---

**Note**: These instructions are specifically tailored for LaCompraGo's architecture and development approach. They emphasize simplicity, minimal dependencies, and practical functionality over complexity.
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
- Manual workflow dispatch with custom version tag

**Outputs:**
- GitHub Release with attached APK file
- APK named as `LaCompraGo-{version}.apk`

**Usage:**
```bash
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
