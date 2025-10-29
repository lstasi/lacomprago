# LaCompraGo - Documentation Index

## Overview

This directory contains the complete architecture and design documentation for the LaCompraGo Android application. The documentation has been simplified to focus on minimal dependencies and straightforward implementation.

## Documentation Structure

### 1. [Architecture Overview](./architecture.md)
**Purpose**: Defines the simplified system architecture

**Contents**:
- Simplified MVVM pattern
- Minimal layer structure
- Component responsibilities
- Minimal technology stack
- JSON file storage
- Simple navigation
- Threading with Coroutines

**Key Decisions**:
- MVVM without complex domain layer
- JSON files instead of Room database
- OkHttp only (no Retrofit)
- Manual dependency management (no Hilt)
- Token-based authentication (no OAuth)
- Sequential order processing

---

### 2. [Data Models](./data-models.md)
**Purpose**: Simplified data structures using JSON

**Contents**:
- Product model (frequency and last purchase)
- ProductList for JSON storage
- ProcessedOrders tracking
- API response models
- UI state models
- JSON serialization with Gson

**Key Models**:
- `Product`: id, name, frequency, lastPurchase
- `ProductList`: List of products in JSON file
- `ProcessedOrders`: Tracks which orders have been processed
- `OrderResponse`: API order data
- `CartRequest`: Shopping cart submission

---

### 3. [API Integration](./api-integration.md)
**Purpose**: Simple API integration with OkHttp

**Contents**:
- OkHttp-only setup (no Retrofit)
- Token interceptor
- API endpoints
- Sequential order processing
- Error handling
- Retry logic
- Progress tracking

**Key Endpoints**:
- Token validation: `/api/validate`
- Order list: `/api/orders`
- Order details: `/api/orders/{id}`
- Create cart: `/api/cart`

---

### 4. [Authentication Flow](./authentication.md)
**Purpose**: Simple token-based authentication

**Contents**:
- Token input screen
- EncryptedSharedPreferences storage
- Token validation
- No OAuth flow
- Simple state machine
- Error handling

**Key Components**:
- `TokenStorage`: Encrypted token persistence
- `TokenValidator`: Validate token with API
- `AuthViewModel`: Manage auth state
- `TokenInterceptor`: Add token to requests

---

### 5. [Feature Specifications](./features.md)
**Purpose**: Detailed specifications for all features

**Contents**:

#### Feature 1: Token Authentication
- Text field for token input
- Encrypted storage
- Validation on first API call

#### Feature 2: Product List
- Display products with frequency and last purchase
- Load from JSON file
- Sort by frequency

#### Feature 3: Order Processing
- Sequential download (one by one)
- Progress display (X of Y orders)
- Cancellable with partial progress save
- Update products after each order
- Track processed orders

#### Feature 4: Shopping Cart
- Auto-generate from frequent products
- Submit to API
- Success/error handling

---

## Design Principles

### 1. Simplicity First
- Minimal dependencies
- No unnecessary features
- Direct implementations
- Easy to understand

### 2. Sequential Processing
- Download orders one at a time
- Update after each order
- Save progress continuously
- Allow cancellation

### 3. File-Based Storage
- JSON files for persistence
- No database complexity
- Easy to debug
- Efficient for this use case

### 4. Security
- Encrypted token storage
- HTTPS communication
- No sensitive data in logs
- Secure file storage

## Technology Stack Summary

### Minimal Dependencies
```gradle
// Android Core (required)
androidx.core:core-ktx
androidx.appcompat:appcompat
com.google.android.material:material

// Lifecycle (ViewModel, LiveData)
androidx.lifecycle:lifecycle-viewmodel-ktx
androidx.lifecycle:lifecycle-livedata-ktx

// Networking (HTTP only)
com.squareup.okhttp3:okhttp

// JSON
com.google.code.gson:gson

// Security
androidx.security:security-crypto
```

### What's NOT Included
- ❌ Hilt/Dagger (manual DI)
- ❌ Room (JSON files)
- ❌ Retrofit (OkHttp only)
- ❌ Navigation Component (simple transitions)
- ❌ DataStore (EncryptedSharedPreferences)
- ❌ Compose (traditional Views)

## Development Workflow

### Phase 1: Setup ✅ COMPLETE
- ✅ Architecture design
- ✅ Documentation creation
- ✅ Simplified approach
- ✅ Task breakdown

### Phase 2: Foundation
- Create Android project
- Add minimal dependencies
- Set up token authentication
- Implement JSON storage

### Phase 3: Features
- Build product list screen
- Implement order processing
- Create cart generation
- Connect to API

### Phase 4: Polish
- Error handling
- Progress indicators
- Testing
- Bug fixes

## Quick Reference

### File Structure (Future)
```
app/
├── src/main/
│   ├── java/com/lacomprago/
│   │   ├── data/
│   │   │   ├── storage/         # JSON file operations
│   │   │   ├── api/             # OkHttp API client
│   │   │   └── model/           # Data models
│   │   ├── ui/
│   │   │   ├── token/           # Token input screen
│   │   │   ├── products/        # Product list screen
│   │   │   ├── processing/      # Order processing dialog
│   │   │   └── cart/            # Cart screen
│   │   └── viewmodel/           # ViewModels
│   └── res/
│       ├── layout/              # XML layouts
│       └── values/              # Strings, colors
└── build.gradle
```

### Data Files
- `products.json`: Product list with frequencies
- `processed_orders.json`: Tracked order IDs
- Token stored in EncryptedSharedPreferences

## Key Differences from Original Design

### Simplified
| Original | Simplified |
|----------|------------|
| OAuth 2.0 | Token input |
| Room database | JSON files |
| Retrofit | OkHttp only |
| Hilt DI | Manual DI |
| Complex flows | Simple flows |
| Parallel processing | Sequential processing |

### Why Simplified?
- Faster development
- Easier to understand
- Fewer dependencies
- Less maintenance
- Sufficient for requirements

## Getting Started with Implementation

1. **Read Architecture Overview** - Understand the simple structure
2. **Review Data Models** - See JSON-based storage
3. **Study API Integration** - Learn OkHttp usage
4. **Check Feature Specs** - Understand each feature
5. **Follow Todo** - Implementation order

## Testing Strategy

### Focus Areas
- Token storage encryption
- JSON file operations
- API calls
- Order processing logic
- Product frequency calculations

### Test Types
- Unit tests for logic
- Integration tests for API
- Manual testing on device

## Common Patterns

### JSON Storage Pattern
```kotlin
// Save
val json = gson.toJson(productList)
context.openFileOutput("products.json", MODE_PRIVATE).use {
    it.write(json.toByteArray())
}

// Load
context.openFileInput("products.json").use { input ->
    val json = input.bufferedReader().use { it.readText() }
    gson.fromJson(json, ProductList::class.java)
}
```

### API Call Pattern
```kotlin
// Build request
val request = Request.Builder()
    .url("$BASE_URL/api/orders")
    .get()
    .build()

// Execute
val response = httpClient.newCall(request).execute()

// Parse
val json = response.body?.string()
val data = gson.fromJson(json, OrderListResponse::class.java)
```

### Sequential Processing Pattern
```kotlin
for ((index, order) in orders.withIndex()) {
    // Check cancellation
    if (!shouldContinue()) break
    
    // Update progress
    onProgress(index + 1, orders.size, order.id)
    
    // Process
    processOrder(order)
    
    // Save progress
    saveProgress()
}
```

## Version History

- **v0.2.0** - Simplified design (Current)
  - Token-based authentication
  - JSON file storage
  - Minimal dependencies
  - Sequential processing

- **v0.1.0** - Initial design
  - OAuth authentication
  - Room database
  - Full dependency stack

---

*Last Updated: October 29, 2025*
*Status: Simplified Design Phase Complete - Ready for Implementation*
