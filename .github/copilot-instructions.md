# GitHub Copilot Instructions for LaCompraGo

## 🎯 Project Overview

**LaCompraGo** is an Android application that automates shopping cart building by analyzing past purchase history. It helps users create intelligent shopping carts based on purchase frequency and patterns.

### Key Information
- **Platform**: Android
- **Language**: Kotlin
- **Target SDK**: Android 14 (API Level 34)
- **Minimum SDK**: Android 14 (API Level 34)
- **Architecture**: Simplified MVVM (Model-View-ViewModel)
- **Development Status**: Currently in design/early implementation phase

## 🏗️ Architecture Principles

### Simplified MVVM Pattern
This project follows a **simplified MVVM architecture** with **minimal dependencies**. The focus is on:
- Simplicity over complexity
- Functionality over aesthetics
- Minimal abstraction layers
- Clean, maintainable code

### Layer Structure
```
Presentation (Activity/Fragment) → ViewModel → Repository → Data Sources (JSON/API)
```

### Key Design Principles
1. **Simplicity First**: Avoid unnecessary complexity, animations, or graphics
2. **Single Responsibility**: Each class has one clear purpose
3. **Minimal Dependencies**: Use only essential libraries
4. **Security**: Encrypted token storage, HTTPS communication
5. **Maintainability**: Clear code organization, easy to understand

## 📦 Dependencies (Minimal Set)

### Core Libraries
- **AndroidX Core & AppCompat**: Standard Android components
- **Material Design**: Basic UI components only
- **ViewModel & LiveData**: State management
- **Coroutines**: Asynchronous operations (Kotlin standard library)

### Networking & Data
- **OkHttp**: Direct HTTP client (no Retrofit)
- **Gson**: JSON serialization/deserialization
- **EncryptedSharedPreferences**: Secure token storage

### What We DON'T Use
- ❌ No Hilt/Dagger (manual dependency management)
- ❌ No Room (JSON files instead)
- ❌ No Retrofit (OkHttp only)
- ❌ No Navigation Component (simple Activity/Fragment transitions)
- ❌ No DataStore (EncryptedSharedPreferences only)
- ❌ No complex animations or graphics libraries

## 💻 Coding Guidelines

### Kotlin Best Practices
- Use Kotlin-idiomatic syntax (data classes, extension functions, sealed classes)
- Prefer immutable data (`val`) over mutable (`var`)
- Use null safety features: safe calls (`?.`), Elvis operator (`?:`), smart casting
- Favor expression-style syntax and scoped functions (`let`, `apply`, `run`, `with`, `also`)
- Keep functions and files concise and focused
- Use sealed classes for state management (Loading, Success, Error states)

### Code Style
- Follow Kotlin coding conventions
- Use meaningful variable and function names
- Document public APIs with KDoc
- Keep business logic out of Activities/Fragments
- Use Coroutines with proper dispatchers (Main for UI, IO for network/file operations)

### Patterns to Follow
✅ Use data classes for models and DTOs
✅ Use sealed classes with `when` expressions for state handling
✅ Use LiveData for observable data in ViewModels
✅ Handle errors gracefully with user-friendly messages
✅ Validate input explicitly
✅ Use proper exception handling

### Patterns to Avoid
❌ Don't use `!!` (force unwrap) - handle nullability explicitly
❌ Avoid exposing mutable state - prefer immutable interfaces
❌ Don't use `lateinit` unless absolutely necessary
❌ Avoid mixing UI and business logic
❌ Don't ignore lifecycle management
❌ Avoid global objects without proper lifecycle management

## 📁 Project Structure

```
app/src/
  main/
    java/com/lacomprago/
      ui/           # Activities, Fragments, UI components
      viewmodel/    # ViewModels for state management
      repository/   # Data coordination layer
      model/        # Data classes and models
      api/          # API client and configuration
      storage/      # JSON file operations, encrypted preferences
      util/         # Utility classes and extensions
    res/
      layout/       # XML layouts (simple, functional)
      values/       # Strings, colors, themes
    AndroidManifest.xml
  test/           # Unit tests
  androidTest/    # Instrumented tests
```

## 🗄️ Data Management

### Local Storage (JSON Files)
- `products.json`: Product list with frequency and last purchase date
- `processed_orders.json`: List of processed order IDs
- All JSON files stored in app-private directory
- Use Gson for serialization/deserialization

### Secure Storage
- Use `EncryptedSharedPreferences` for API token storage
- Never log sensitive data (tokens, user information)
- All API communication over HTTPS

### API Integration
- Use OkHttp for HTTP requests
- Token-based authentication (Bearer token)
- Handle network errors gracefully
- Implement proper timeout configurations
- Process orders sequentially, one at a time

## 🎨 UI Guidelines

### Design Philosophy
- Simple, functional UI - no fancy animations
- Basic Material Design components
- Focus on usability and clarity
- Progress indicators for long-running operations
- Cancellable operations where appropriate

### UI Components
- Use ConstraintLayout for layouts
- View binding for type-safe view access
- Display loading states clearly
- Show progress for order processing
- Provide user feedback for all actions

## 🧪 Testing Strategy

### Unit Tests
- Test ViewModel logic and state management
- Test Repository implementations
- Test JSON parsing and serialization
- Test product frequency calculations
- Use JUnit for unit tests

### Integration Tests
- Test API calls (with mock server or test token)
- Test JSON file operations
- Test complete order processing flow
- Use AndroidX Test framework

### Test Structure
- Follow AAA pattern (Arrange-Act-Assert)
- Test edge cases and error conditions
- Mock external dependencies appropriately
- Use descriptive test names

## 🔐 Security Considerations

### Authentication
- Simple token-based authentication
- Token encrypted at rest using EncryptedSharedPreferences
- Token validated on first API call
- Clear token on logout

### Data Protection
- No sensitive data in logs
- HTTPS for all API communications
- JSON files in app-private directory only
- Proper permission handling

## ⚡ Performance Guidelines

### Optimization Strategies
- Process orders sequentially (one at a time) to manage memory
- Incremental JSON file updates
- Proper coroutine cancellation support
- Release resources after each operation
- Use appropriate Coroutine dispatchers

### Threading
- **Main Thread**: UI operations only
- **IO Dispatcher**: Network and file I/O operations
- **Default Dispatcher**: CPU-intensive operations (if needed)

## 📚 Documentation

### What to Document
- Public classes and functions (use KDoc)
- Complex business logic
- Architecture decisions (see `/doc` folder)
- API endpoints and data models
- State management patterns

### Existing Documentation
- `/doc/architecture.md`: Detailed architecture overview
- `/doc/data-models.md`: Data structure specifications
- `/doc/api-integration.md`: API documentation
- `/doc/authentication.md`: Auth flow details
- `/doc/features.md`: Feature specifications
- `README.md`: Project overview and setup
- `BUILD_NOTES.md`: Build and setup information

## 🔧 Build & Development

### Build Commands
```bash
# Clean build
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Run tests
./gradlew test

# Run instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest
```

### Development Requirements
- Android Studio (latest stable)
- JDK 17 or higher
- Android SDK 34 (Android 14)
- Kotlin 1.9+ (specified in build.gradle.kts)

## 🎯 Feature Development Guidelines

When implementing features:
1. Start with the ViewModel and define states (sealed classes)
2. Implement Repository layer for data operations
3. Create/update UI components
4. Wire everything together with LiveData observers
5. Add appropriate error handling
6. Test the complete flow
7. Update documentation if needed

## 🚀 Common Development Tasks

### Adding a New Screen
1. Create Activity/Fragment in `ui/` package
2. Create corresponding ViewModel in `viewmodel/` package
3. Define UI states using sealed classes
4. Create layout XML in `res/layout/`
5. Set up View binding
6. Implement LiveData observers
7. Add navigation logic

### Adding a New API Endpoint
1. Define data models in `model/` package
2. Add endpoint configuration in `api/` package
3. Implement in Repository layer
4. Update ViewModel to handle new data
5. Update UI to display results
6. Add error handling
7. Add tests

### Adding Local Data Storage
1. Define data structure (data class)
2. Create JSON schema
3. Implement read/write methods in `storage/` package
4. Add to Repository layer
5. Test serialization/deserialization
6. Handle file I/O errors

## 💡 Tips for Working with Copilot

### Effective Prompts
- Be specific about the architecture layer you're working in
- Mention that we use simplified MVVM with minimal dependencies
- Specify that we DON'T use certain libraries (Retrofit, Room, etc.)
- Reference existing patterns in the codebase
- Ask for Kotlin-idiomatic solutions

### Example Prompts
- "Create a ViewModel for managing product list with sealed class states"
- "Implement a Repository method to read products from JSON using Gson"
- "Write an OkHttp request with token authentication and error handling"
- "Create a data class for Order with Gson annotations"
- "Add a LiveData observer in Activity with loading and error states"

## 🔄 Workflow

1. **Plan**: Understand the requirement and architecture impact
2. **Implement**: Write clean, simple, tested code
3. **Test**: Unit and integration tests as appropriate
4. **Document**: Update documentation for significant changes
5. **Review**: Ensure code follows project guidelines
6. **Build**: Verify the app builds and runs correctly

## 📖 References

- [Kotlin Documentation](https://kotlinlang.org/docs/home.html)
- [Android Developers Guide](https://developer.android.com/)
- [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- [Android Architecture Components](https://developer.android.com/topic/libraries/architecture)
- [OkHttp Documentation](https://square.github.io/okhttp/)
- [Gson Documentation](https://github.com/google/gson)

---

**Remember**: Simplicity and functionality over complexity. We're building a practical tool, not showcasing fancy features.
