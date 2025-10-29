# LaCompraGo - Project Tasks

## Phase 1: Design & Architecture ✅ COMPLETE

- [x] Create project documentation structure
- [x] Define simplified architecture overview
- [x] Design data models (JSON-based)
- [x] Plan API integration strategy (OkHttp only)
- [x] Design token authentication flow
- [x] Specify features and requirements

## Phase 2: Project Setup

- [ ] Create Android project with Kotlin
- [ ] Set up Gradle configuration (minimal dependencies)
- [ ] Configure target SDK (Android 14)
- [ ] Add minimal dependencies:
  - [ ] AndroidX Core, AppCompat, Material
  - [ ] Lifecycle (ViewModel, LiveData)
  - [ ] OkHttp
  - [ ] Gson
  - [ ] Security (EncryptedSharedPreferences)
- [ ] Configure build variants (debug/release)
- [ ] Set up basic project structure

## Phase 3: Token Authentication

- [ ] Create token input UI screen
- [ ] Implement EncryptedSharedPreferences for token storage
- [ ] Create TokenStorage class
- [ ] Create TokenValidator class
- [ ] Implement AuthViewModel
- [ ] Add token validation with API
- [ ] Handle token errors
- [ ] Implement token clear functionality

## Phase 4: JSON Storage

- [ ] Create JsonStorage class
- [ ] Implement Product data model
- [ ] Implement ProductList data model
- [ ] Implement ProcessedOrders data model
- [ ] Create save/load methods for products.json
- [ ] Create save/load methods for processed_orders.json
- [ ] Add error handling for file I/O
- [ ] Test JSON serialization/deserialization

## Phase 5: API Integration

- [ ] Create ApiClient class with OkHttp
- [ ] Implement TokenInterceptor
- [ ] Create API endpoints:
  - [ ] Validate token
  - [ ] Get order list
  - [ ] Get order details
  - [ ] Create cart
- [ ] Implement request/response models
- [ ] Add API error handling
- [ ] Implement retry logic
- [ ] Test API calls

## Phase 6: Product List Feature

- [ ] Create Product list UI screen
- [ ] Implement ProductViewModel
- [ ] Load products from JSON on app start
- [ ] Display products with frequency and last purchase
- [ ] Sort products by frequency
- [ ] Implement empty state
- [ ] Add "Refresh" button
- [ ] Show product count

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
