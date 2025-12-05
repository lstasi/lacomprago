# LaCompraGo - Architecture Overview

## Architecture Pattern: Simplified MVVM

### Overview

LaCompraGo follows a simplified MVVM (Model-View-ViewModel) architecture pattern with minimal dependencies. The focus is on simplicity, keeping only essential components for functionality.

## Architecture Layers

```
┌─────────────────────────────────────────────────────────┐
│                     Presentation Layer                   │
│  ┌──────────────┐  ┌──────────────┐                     │
│  │   Activity   │  │   Fragment   │                     │
│  └──────────────┘  └──────────────┘                     │
│          │                  │                            │
│          └──────────────────┘                            │
│                           ↓                              │
│  ┌───────────────────────────────────────────────────┐  │
│  │              ViewModel Layer                       │  │
│  │  • State Management                                │  │
│  │  • Business Logic                                  │  │
│  │  • LiveData emissions                              │  │
│  └───────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────┐
│                      Data Layer                          │
│  ┌───────────────────────────────────────────────────┐  │
│  │              Repository (Simple)                   │  │
│  │  • Data coordination                               │  │
│  │  • Business logic for data processing              │  │
│  └───────────────────────────────────────────────────┘  │
│          │                                      │        │
│          ↓                                      ↓        │
│  ┌──────────────┐                    ┌──────────────┐   │
│  │  JSON Files  │                    │  Remote API  │   │
│  │              │                    │  (OkHttp)    │   │
│  └──────────────┘                    └──────────────┘   │
└─────────────────────────────────────────────────────────┘
```

## Component Descriptions

### 1. Presentation Layer

**Activities & Fragments**
- Host UI components
- Handle navigation
- Observe ViewModel state
- Manage lifecycle events

**UI Components**
- Simple, no animations
- Basic Material Design components
- Focus on functionality over aesthetics
- Progress indicators for order processing
- Cancellable operations

### 2. ViewModel Layer

**Responsibilities**
- Manage UI state
- Handle user interactions
- Coordinate data operations
- Survive configuration changes
- Expose data via LiveData

**Key ViewModels**
- `TokenViewModel`: Manage token input and storage
- `ProductViewModel`: Handle product list and processing
- `OrderProcessingViewModel`: Manage order download and processing
- `CartViewModel`: Manage cart creation and submission

### 3. Data Layer

**Repository (Simplified)**
- `TokenRepository`: Handle token storage (encrypted)
- `ProductRepository`: Manage product list JSON file
- `OrderRepository`: Download orders and update products
- `ProcessedOrdersRepository`: Track which orders have been processed

**Local Storage (JSON Files)**
- `products.json`: Product list with frequency and last purchase
- `processed_orders.json`: List of processed order IDs
- `token.dat`: Encrypted token storage

**Remote API (OkHttp Only)**
- Simple HTTP client
- Token-based authentication
- Fetch orders one by one
- Submit shopping cart
- Basic error handling

## Key Technologies (Minimal Dependencies)

### Core
- **Kotlin**: Primary language
- **Coroutines**: Asynchronous operations (Kotlin standard library)

### Android Standard
- **ViewModel**: UI state management (AndroidX)
- **LiveData**: Observable data holders (AndroidX)
- **Lifecycle**: Lifecycle-aware components (AndroidX)

### Networking (Minimal)
- **OkHttp**: HTTP client (no Retrofit)
- **Gson**: JSON serialization

### Security (Standard Android)
- **EncryptedSharedPreferences**: Secure token storage (AndroidX Security)

### No Additional Dependencies
- No Hilt/Dagger (manual dependency management)
- No Room (JSON files instead)
- No Retrofit (OkHttp only)
- No Navigation Component (simple Activity/Fragment transitions)
- No DataStore (EncryptedSharedPreferences only)

## Design Principles

### 1. Simplicity First
- Minimal dependencies
- No unnecessary animations
- No complex graphics
- Focus on functionality
- Clean, minimal UI

### 2. Single Responsibility
- Each class has one clear purpose
- Simple data flow

### 3. Maintainability
- Clear code organization
- Easy to understand
- Minimal abstraction layers

### 4. Security
- Encrypted token storage
- Secure API communication (HTTPS)
- No sensitive data logging

## Data Storage

### JSON File Structure

**products.json**
```json
{
  "products": [
    {
      "id": "prod_123",
      "name": "Milk 1L",
      "frequency": 24,
      "lastPurchase": 1698765432000,
      "category": "Dairy"
    }
  ]
}
```

**processed_orders.json**
```json
{
  "processedOrderIds": [
    "order_123",
    "order_456"
  ]
}
```

## Navigation Structure

```
Token Input Screen
     ↓
Product List Screen
     ├── Refresh Products (with progress)
     │        ├── Show processing status
     │        ├── Allow cancellation
     │        └── Update product list
     └── Create Shopping Cart
              └── Submit to API
```

### Debug Mode Screen (Debug builds only)
```
Main Screen
     ↓ (long press version, debug builds only)
Debug Mode Screen
     ├── Token Management
     │        ├── Token input/paste
     │        ├── Customer ID input
     │        └── Token validation
     ├── Endpoint Testing
     │        ├── Select endpoint
     │        ├── Configure parameters
     │        ├── Send request
     │        └── View response
     └── Response Viewer
              ├── Status code
              ├── Headers
              └── Body (formatted JSON)
```

See [debug-mode.md](./debug-mode.md) for detailed specification.

## State Management

### UI State
- Simple sealed classes for states
- Loading, Success, Error states
- Processing state with progress

### Data Flow
```
User Action → ViewModel → Repository → API/JSON Files
                  ↑                          ↓
                  └──────── LiveData ────────┘
```

## Order Processing Flow

### Sequential Order Download
1. User triggers "Refresh Products"
2. Fetch list of order IDs from API
3. Filter out already processed orders
4. For each unprocessed order:
   - Download order details
   - Extract products
   - Update product frequency and last purchase
   - Add order ID to processed list
   - Update UI with progress (count)
   - Check for cancellation
5. Save updated product list to JSON
6. Save processed orders to JSON

### Progress Tracking
- Show current order being processed
- Show total count of orders
- Display progress indicator
- Allow user to cancel at any time
- Persist partial progress

## Error Handling

### Strategy
- Network errors: Show message, allow retry
- Token errors: Prompt for valid token
- Data errors: Log and skip problematic orders
- User-friendly error messages

### Error Types
- Network errors
- Authentication errors (invalid token)
- JSON parsing errors
- File I/O errors

## Threading Model

- **Main Thread**: UI operations only
- **IO Dispatcher**: Network and file operations
- **Coroutines**: Simple async operations

## Security Considerations

### Data Security
- Token encrypted at rest using EncryptedSharedPreferences
- HTTPS for all API communications
- No sensitive data in logs
- JSON files stored in app-private directory

### Authentication
- Simple token-based authentication
- Token input via text field
- Secure token storage
- Token validation on first API call

## Performance Considerations

### Optimization
- Sequential order processing (one at a time)
- Incremental JSON file updates
- Memory-efficient order processing
- Cancellable operations

### Memory Management
- Process orders one by one to minimize memory
- Release resources after each order
- Proper coroutine cancellation

## Testing Strategy

### Unit Tests
- ViewModel logic
- Repository implementations
- JSON parsing/serialization
- Product frequency calculations

### Integration Tests
- API calls with real token
- JSON file operations
- Order processing flow

## Build Configuration

### Gradle Structure
```
project/
├── build.gradle (root)
├── app/
│   ├── build.gradle (app module - minimal dependencies)
│   └── src/
│       ├── main/
│       ├── test/
│       └── androidTest/
```

### Minimal Dependencies
```gradle
dependencies {
    // Android Core
    implementation 'androidx.core:core-ktx'
    implementation 'androidx.appcompat:appcompat'
    implementation 'com.google.android.material:material'
    
    // Lifecycle
    implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx'
    implementation 'androidx.lifecycle:lifecycle-livedata-ktx'
    
    // Networking
    implementation 'com.squareup.okhttp3:okhttp'
    
    // JSON
    implementation 'com.google.code.gson:gson'
    
    // Security
    implementation 'androidx.security:security-crypto'
    
    // Testing
    testImplementation 'junit:junit'
    androidTestImplementation 'androidx.test.ext:junit'
}
```

## Conclusion

This simplified architecture provides a solid foundation for LaCompraGo application, emphasizing simplicity, minimal dependencies, and maintainability while following basic Android best practices.
