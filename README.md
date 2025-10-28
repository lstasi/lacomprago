# LaComprago

Automated shopping cart builder for Android

## Overview

LaComprago is an Android application that helps users automate their shopping experience by analyzing past purchase history and creating intelligent shopping carts based on purchase frequency and patterns.

## Features

- **OAuth Authentication**: Secure login using OAuth tokens
- **Order History**: Download and view past orders from supermarket API
- **Statistical Analysis**: Analyze purchase patterns and product frequency
- **Product Tracking**: Track how often products are purchased and when they were last bought
- **Smart Shopping Cart**: Automatically create shopping carts based on purchase history

## Technical Details

- **Platform**: Android
- **Target SDK**: Android 14 (API Level 34)
- **Language**: Kotlin
- **Architecture**: MVVM (Model-View-ViewModel)
- **Minimum SDK**: Android 14 (No backward compatibility)

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
3. Sync Gradle dependencies
4. Configure OAuth credentials (details in authentication documentation)
5. Build and run the application

## Development Status

This project is currently in the design phase. Architecture and documentation are being created before implementation begins.

## License

[To be determined]
