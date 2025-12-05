# LaCompraGo - Project Tasks

> ⚠️ **IMPORTANT**: This project follows a **granular, test-first development approach**.
> Each phase must be completed and tested before moving to the next.
> API endpoints must be validated in Debug Mode before integration.

## Documentation

- [Architecture Overview](./doc/architecture.md)
- [Data Models](./doc/data-models.md)
- [API Integration](./doc/api-integration.md)
- [Mercadona API Reference](./doc/mercadona-api.md) ← **NEW: Real API endpoints**
- [Authentication Flow](./doc/authentication.md)
- [Feature Specifications](./doc/features.md)
- [Debug Mode Specification](./doc/debug-mode.md) ← **NEW: API testing UI**

---

## Phase 1: Design & Architecture ✅ COMPLETE

- [x] Create project documentation structure
- [x] Define simplified architecture overview
- [x] Design data models (JSON-based)
- [x] Plan API integration strategy (OkHttp only)
- [x] Design token authentication flow
- [x] Specify features and requirements

---

## Phase 2: Project Setup ✅ COMPLETE

- [x] Create Android project with Kotlin
- [x] Set up Gradle configuration (minimal dependencies)
- [x] Configure target SDK (Android 14)
- [x] Add minimal dependencies:
  - [x] AndroidX Core, AppCompat, Material
  - [x] Lifecycle (ViewModel, LiveData)
  - [x] OkHttp
  - [x] Gson
  - [x] Security (EncryptedSharedPreferences)
- [x] Configure build variants (debug/release)
- [x] Set up basic project structure
- [x] Set up repository configuration:
  - [x] GitHub Actions for CI/CD
  - [x] Dependabot with Android 34 version pinning
  - [x] Release build workflow with APK artifacts

---

## Phase 3: API Refactor & Validation ✅ COMPLETE

> **Status**: Refactored to real Mercadona API endpoints with validation documented and Debug Mode coverage in place.

### 3.1 Documentation Updates
- [x] Document real Mercadona API endpoints (see [mercadona-api.md](./doc/mercadona-api.md))
- [x] Create Debug Mode specification (see [debug-mode.md](./doc/debug-mode.md))
- [x] Update api-integration.md with real endpoints
- [x] Update data-models.md with Mercadona response structures

### 3.2 API Client Refactor
- [x] Update ApiConfig with Mercadona base URL
- [x] Update data models to match Mercadona API responses:
  - [x] Customer response model
  - [x] Order list response model (with pagination)
  - [x] Order details response model
  - [x] Cart response model
  - [x] Recommendations response model
- [x] Update ApiClient methods:
  - [x] Add customer_id parameter to all authenticated endpoints
  - [x] Update getOrderList() for Mercadona pagination
  - [x] Update getOrderDetails() with correct path
  - [x] Add getCustomerInfo() for token validation
  - [x] Add getCart() method
  - [x] Add getRecommendations() method
- [x] Add rate limiting (max 60 requests/minute)
- [x] Add strict validation:
  - [x] Validate token format before requests
  - [x] Validate customer_id before requests
  - [x] Validate response structure
  - [x] Add request cooldown (1-2 seconds between requests)

### 3.3 Debug Mode Implementation
- [x] Create DebugActivity (debug builds only)
- [x] Implement token management section:
  - [x] Token input/paste field
  - [x] Clear token button
  - [x] Token status display
  - [x] Customer ID input field
- [x] Implement endpoint list:
  - [x] GET /customers/{id}/ (Customer Info)
  - [x] GET /customers/{id}/cart/ (Get Cart)
  - [x] GET /customers/{id}/orders/ (List Orders)
  - [x] GET /customers/{id}/orders/{orderId}/ (Order Details)
  - [x] GET /customers/{id}/recommendations/myregulars/{type}/ (Recommendations)
  - [x] PUT /postal-codes/actions/change-pc/ (Set Warehouse)
- [x] Implement request configuration:
  - [x] Path parameter input
  - [x] Query parameter input
  - [x] Request body editor (for PUT/POST)
- [x] Implement request preview:
  - [x] Show full URL
  - [x] Show headers
  - [x] Show body
- [x] Implement response viewer:
  - [x] Status code and message
  - [x] Response time
  - [x] Response headers
  - [x] Response body (formatted JSON)
  - [x] Copy response button
- [x] Add hidden access from main screen (debug builds only)

### 3.4 Endpoint Validation
Validate each endpoint in Debug Mode before integration:
- [x] Customer Info endpoint
- [x] List Orders endpoint (with pagination)
- [x] Get Order Details endpoint
- [x] Get Cart endpoint
- [x] Get Recommendations endpoint
- [x] Set Warehouse endpoint

---

## Phase 4: Token Authentication ✅ COMPLETE (Needs validation)

- [x] Create token input UI screen
- [x] Implement EncryptedSharedPreferences for token storage
- [x] Create TokenStorage class
- [x] Create TokenValidator class
- [x] Implement AuthViewModel
- [x] Handle token errors
- [x] Implement token clear functionality

### 4.1 Token Validation Updates (After Debug Mode)
- [ ] Update token validation to use Customer Info endpoint
- [ ] Store customer_id along with token
- [ ] Validate token on app start
- [ ] Test with real Mercadona token in Debug Mode

---

## Phase 5: JSON Storage ✅ COMPLETE

- [x] Create JsonStorage class
- [x] Implement Product data model
- [x] Implement ProductList data model
- [x] Implement ProcessedOrders data model
- [x] Create save/load methods for products.json
- [x] Create save/load methods for processed_orders.json
- [x] Add error handling for file I/O
- [x] Test JSON serialization/deserialization

---

## Phase 6: Order Processing Feature ⚠️ NEEDS VALIDATION

> **Status**: Implementation complete but not validated with real API.

- [x] Create order processing dialog UI
- [x] Implement OrderProcessingViewModel
- [x] Fetch list of orders from API
- [x] Filter out processed orders
- [x] Implement sequential order download
- [x] Show progress (current/total, order ID)
- [x] Implement cancellation
- [x] Handle errors (skip order and continue)
- [x] Show completion/cancelled message

### 6.1 Order Processing Validation (After Debug Mode)
- [ ] Test List Orders endpoint in Debug Mode
- [ ] Test Order Details endpoint in Debug Mode
- [ ] Update order processing to use customer_id
- [ ] Update data models for Mercadona order structure
- [ ] Validate product extraction from real orders
- [ ] Test with real orders

---

## Phase 7: Product List Feature ✅ COMPLETE

- [x] Create Product list UI screen
- [x] Implement ProductViewModel
- [x] Load products from JSON on app start
- [x] Display products with frequency and last purchase
- [x] Sort products by frequency
- [x] Implement empty state
- [x] Add "Refresh" button
- [x] Show product count

---

## Phase 8: Recommendations Integration (NEW)

> **Priority**: After Order Processing validation

- [ ] Test Recommendations endpoint in Debug Mode
- [ ] Create recommendation data models
- [ ] Implement recommendation fetching
- [ ] Compare recommendations with local product frequency
- [ ] Display recommendations in UI

---

## Phase 9: Shopping Cart Feature

> **Priority**: After Recommendations

- [ ] Test Cart endpoint in Debug Mode
- [ ] Create cart data models for Mercadona API
- [ ] Implement CartViewModel
- [ ] Generate cart from top products + recommendations
- [ ] Display cart items for review
- [ ] Implement cart update:
  - [ ] Build cart update request
  - [ ] Send to Mercadona API
  - [ ] Handle response
- [ ] Show success/error messages
- [ ] Handle cart errors

---

## Phase 10: Unit Testing

- [ ] Write unit tests:
  - [ ] TokenStorage tests
  - [ ] JsonStorage tests
  - [ ] Product update logic tests
  - [ ] Frequency calculation tests
  - [ ] ViewModel tests
  - [ ] API response parsing tests
- [ ] Write integration tests:
  - [ ] API client tests (with mock server)
  - [ ] Order processing flow tests
  - [ ] JSON file operations tests

---

## Phase 11: Manual Testing & Validation

- [ ] Test on Android 14 device
- [ ] Validate all endpoints in Debug Mode
- [ ] Test token input flow with real token
- [ ] Test order processing with real orders
- [ ] Test cancellation
- [ ] Test cart creation
- [ ] Test error handling
- [ ] Test rate limiting behavior

---

## Phase 12: Polish & Bug Fixes

- [ ] Code review and cleanup
- [ ] Fix any bugs found in testing
- [ ] Improve error messages
- [ ] Add loading states
- [ ] Optimize performance
- [ ] Verify all edge cases
- [ ] Update documentation

---

## Phase 13: Release Preparation

- [ ] Configure ProGuard/R8
- [ ] Test release build
- [ ] Verify Debug Mode is hidden in release
- [ ] Verify security (token encryption)
- [ ] Final testing on real device
- [ ] Prepare release notes
- [ ] Create signed APK

---

## Backlog / Future Enhancements

- [ ] Login with email/password (replace token paste)
- [ ] Add product search
- [ ] Add product categories filter
- [ ] Manual cart editing
- [ ] Product details screen
- [ ] Settings screen
- [ ] Dark theme
- [ ] Multiple language support
- [ ] Export product list
- [ ] Import/export cart
- [ ] Backup/restore data
- [ ] Checkout flow integration

---

## Development Guidelines

### Test-First Approach
1. Validate each endpoint in Debug Mode before integration
2. Write unit tests for new functionality
3. Test on real device before marking complete

### Rate Limiting
- Maximum 60 requests per minute
- 1-2 second delay between sequential requests
- Implement exponential backoff for retries

### API Client Requirements
- Validate token format before requests
- Require customer_id for authenticated endpoints
- Parse and validate response structure
- Log requests/responses in debug builds only

### Security
- Never log token values
- Use EncryptedSharedPreferences for sensitive data
- Exclude debug features from release builds
