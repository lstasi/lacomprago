# LaCompraGo - Project Tasks

## Phase 1: Design & Architecture ✅ COMPLETE

- [x] Create project documentation structure
- [x] Define simplified architecture overview
- [x] Design data models (JSON-based)
- [x] Plan API integration strategy (OkHttp only)
- [x] Design token authentication flow
- [x] Specify features and requirements

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

## Phase 3: Token Authentication ✅ COMPLETE

- [x] Create token input UI screen
- [x] Implement EncryptedSharedPreferences for token storage
- [x] Create TokenStorage class
- [x] Create TokenValidator class
- [x] Implement AuthViewModel
- [x] Add token validation with API
- [x] Handle token errors
- [x] Implement token clear functionality

## Phase 4: JSON Storage ✅ COMPLETE

- [x] Create JsonStorage class
- [x] Implement Product data model
- [x] Implement ProductList data model
- [x] Implement ProcessedOrders data model
- [x] Create save/load methods for products.json
- [x] Create save/load methods for processed_orders.json
- [x] Add error handling for file I/O
- [x] Test JSON serialization/deserialization

## Phase 5: API Integration ✅ COMPLETE

- [x] Create ApiClient class with OkHttp
- [x] Implement TokenInterceptor
- [x] Create API endpoints:
  - [x] Validate token
  - [x] Get order list
  - [x] Get order details
  - [x] Create cart
- [x] Implement request/response models
- [x] Add API error handling
- [x] Implement retry logic
- [x] Test API calls

## Phase 6: Product List Feature ✅ COMPLETE

- [x] Create Product list UI screen
- [x] Implement ProductViewModel
- [x] Load products from JSON on app start
- [x] Display products with frequency and last purchase
- [x] Sort products by frequency
- [x] Implement empty state
- [x] Add "Refresh" button
- [x] Show product count

## Phase 7: Order Processing Feature

- [ ] Create order processing dialog UI
- [ ] Implement OrderProcessingViewModel
- [ ] Fetch list of orders from API
- [ ] Filter out processed orders
- [ ] Implement sequential order download:
  - [ ] Download order one at a time
  - [ ] Extract products from order
  - [ ] Update product frequency and last purchase
  - [ ] Save products.json after each order
  - [ ] Mark order as processed
  - [ ] Save processed_orders.json
- [ ] Show progress (current/total, order ID)
- [ ] Implement cancellation:
  - [ ] Cancel button
  - [ ] Stop processing
  - [ ] Save partial progress
- [ ] Handle errors (skip order and continue)
- [ ] Show completion/cancelled message

## Phase 8: Shopping Cart Feature

- [ ] Create cart preview UI screen
- [ ] Implement CartViewModel
- [ ] Generate cart from top products
- [ ] Select products by frequency
- [ ] Calculate suggested quantities
- [ ] Display cart items for review
- [ ] Implement cart submission:
  - [ ] Build CartRequest
  - [ ] Send to API
  - [ ] Handle response
- [ ] Show success/error messages
- [ ] Handle cart creation errors

## Phase 9: Testing

- [ ] Write unit tests:
  - [ ] TokenStorage tests
  - [ ] JsonStorage tests
  - [ ] Product update logic tests
  - [ ] Frequency calculation tests
  - [ ] ViewModel tests
- [ ] Write integration tests:
  - [ ] API client tests
  - [ ] Order processing flow tests
  - [ ] JSON file operations tests
- [ ] Manual testing:
  - [ ] Test on Android 14 device
  - [ ] Test token input flow
  - [ ] Test order processing
  - [ ] Test cancellation
  - [ ] Test cart creation

## Phase 10: Polish & Bug Fixes

- [ ] Code review and cleanup
- [ ] Fix any bugs found in testing
- [ ] Improve error messages
- [ ] Add loading states
- [ ] Optimize performance
- [ ] Verify all edge cases
- [ ] Update documentation

## Phase 11: Release Preparation

- [ ] Configure ProGuard/R8
- [ ] Test release build
- [ ] Verify security (token encryption)
- [ ] Final testing on real device
- [ ] Prepare release notes
- [ ] Create signed APK

## Backlog / Future Enhancements

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
