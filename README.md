# LaCompraGo

Automated shopping cart builder for Android

## Overview

LaCompraGo is a simple Android application that helps users automate their shopping experience by analyzing past purchase history and creating intelligent shopping carts based on purchase frequency and patterns.

## Features

- **Token Authentication**: Simple token input for API access
- **Order Processing**: Download and process past orders one by one
- **Product Frequency Analysis**: Track how often products are purchased and when they were last bought
- **Smart Shopping Cart**: Automatically create shopping carts based on purchase history
- **Debug Mode Validation**: Debug-only screen to exercise Mercadona endpoints, inspect requests, and view formatted responses

## Current Status

- Phase 1 (Design) ✅
- Phase 2 (Project Setup) ✅
- Phase 3 (API Refactor & Validation) ✅ — Mercadona endpoints, models, rate limiting, and Debug Mode coverage documented and validated

## Technical Details

- **Platform**: Android
- **Target SDK**: Android 14 (API Level 34)
- **Language**: Kotlin
- **Architecture**: Simple MVVM pattern
- **Minimum SDK**: Android 14 (No backward compatibility)
- **Storage**: JSON files for local data
- **Dependencies**: Minimal (only essential libraries)

## Project Structure

```
lacomprago/
├── app/                    # Main application module
├── doc/                    # Architecture and design documentation
├── README.md              # This file
├── todo.md                # Project tasks and milestones
└── changelog.md           # Version history and changes
```

## Documentation

See the [doc](./doc/) folder for detailed architecture and design documentation:
- [Architecture Overview](./doc/architecture.md)
- [Data Models](./doc/data-models.md)
- [API Integration](./doc/api-integration.md)
- [Authentication Flow](./doc/authentication.md)
- [Feature Specifications](./doc/features.md)

## Getting Started

### Prerequisites

- Android Studio (latest stable version)
- JDK 17 or higher
- Android SDK 34 (Android 14)
- Kotlin 1.9+

### Setup

1. Clone the repository
2. Open the project in Android Studio
3. Sync Gradle dependencies (minimal dependencies)
4. Input your API token in the app
5. Build and run the application

## CI/CD & Releases

This project uses GitHub Actions for continuous integration and automated releases:

- **Automated Builds**: Every push triggers building of debug and release APKs
- **Testing**: Automated unit tests and lint checks
- **Releases**: Tagged releases automatically create GitHub releases with installable APKs
- **Dependabot**: Automatic dependency updates with Android 34 compatibility maintained

See [.github/README.md](./.github/README.md) for detailed CI/CD documentation.

### Installing APKs

Download APKs from:
- **GitHub Actions**: Actions tab → Select workflow run → Download artifacts
- **Releases**: Releases page → Download APK from any release

Requirements:
- Android 14 (API Level 34) or higher
- Enable "Install from unknown sources" for installation

## Development Status

Documentation through Phase 3 (API refactor and validation) is complete, with implementation and validation tasks moving forward using the outlined Debug Mode.

## License

[To be determined]
