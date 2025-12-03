# Changelog

All notable changes to LaCompraGo will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added (Phase 5: API Integration)
- ApiClient class with OkHttp for HTTP communication
- TokenInterceptor for automatic token attachment to requests
- API endpoint implementations:
  - validateToken() for token validation
  - getOrderList() for fetching order summaries
  - getOrderDetails() for fetching full order details
  - createCart() for creating shopping carts
- API request/response models:
  - OrderSummary, OrderListResponse for order list
  - OrderItemResponse, OrderResponse for order details
  - CartItemRequest, CartRequest, CartResponse for cart operations
  - ValidateTokenResponse for token validation
- ApiException for API error representation
- ApiError sealed class with error categorization and user-friendly messages
- RetryHelper with exponential backoff for network error recovery
- Unit tests for API models and error handling

### Fixed
- TokenValidator now correctly accepts JWT tokens with periods (.) in the format header.payload.signature

### Added (Phase 3: Token Authentication)
- Token input UI screen with Material Design components
- TokenStorage class with EncryptedSharedPreferences for secure token storage
- TokenValidator class for basic token format validation
- AuthState sealed class for authentication state management
- AuthViewModel for managing authentication flow
- Token clear functionality
- Unit tests for TokenValidator

### Changed
- Updated Android Gradle Plugin from 8.5.2 to 8.7.3 for better Android 34 support
- Removed deprecated uses-sdk from AndroidManifest.xml (SDK versions managed in build.gradle.kts)
- Updated MainActivity to handle token input and validation with view binding

### Added (Phase 2: Project Setup)
- Android project structure with Kotlin
- Gradle build configuration with Android Gradle Plugin 8.7.3
- All minimal dependencies (AndroidX, Lifecycle, OkHttp, Gson, Security Crypto)
- Android 14 (API 34) target configuration
- MainActivity as entry point
- Basic UI resources (layouts, strings, colors, themes)
- ProGuard rules for production builds
- Example unit and instrumented tests
- Gradle wrapper 8.11.1
- BUILD_NOTES.md documentation for setup and build instructions

### Changed
- Simplified architecture to use minimal dependencies
- Changed from OAuth to simple token-based authentication
- Changed from Room database to JSON file storage
- Changed from Retrofit to OkHttp only
- Removed Hilt dependency injection (manual DI)
- Simplified order processing to sequential (one by one)

### Added
- Token input screen for simple authentication
- Sequential order processing with progress tracking
- Cancellable order processing with partial progress save
- JSON file storage for products and processed orders
- Product frequency and last purchase tracking
- Shopping cart auto-generation from frequent products

### Documentation
- Complete architecture redesign for simplicity
- Simplified data models (JSON-based)
- Token-based authentication flow
- Simplified API integration (OkHttp only)
- Updated feature specifications
- Updated task breakdown in todo.md

## [0.1.0] - TBD (Design Phase)

### Project Initialization
- Repository created
- Basic project structure defined
- Target platform: Android 14 (API Level 34)
- Language: Kotlin
- Architecture: Simplified MVVM with minimal dependencies

### Notes
- This version represents the design and planning phase
- No executable code has been implemented yet
- Focus on creating simple, maintainable architecture

---

## Version History Guidelines

### Types of Changes
- **Added** for new features
- **Changed** for changes in existing functionality
- **Deprecated** for soon-to-be removed features
- **Removed** for now removed features
- **Fixed** for any bug fixes
- **Security** in case of vulnerabilities

### Version Numbering
- **Major**: Incompatible API changes or major feature releases
- **Minor**: New features, backwards-compatible
- **Patch**: Backwards-compatible bug fixes
