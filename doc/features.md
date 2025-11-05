# LaCompraGo - Feature Specifications

## Overview

This document provides detailed specifications for each feature of the simplified LaCompraGo application.

## Feature 1: Token Authentication

### User Story
As a user, I want to input my API token, so that I can access the supermarket API to manage my shopping.

### Requirements

**Functional Requirements**
- User can input/paste token in a text field
- Token is validated on first API call
- Token is stored encrypted locally
- User can clear token and input new one

**Non-Functional Requirements**
- Token must be stored encrypted
- Session persists across app restarts
- No sensitive data in logs

### User Flow

```
1. User opens app
2. App checks for stored token
   ├─ Token exists → Navigate to Product List
   └─ No token → Show Token Input Screen
3. User pastes/types token
4. User taps "Submit"
5. App validates token with API call
   ├─ Valid → Navigate to Product List
   └─ Invalid → Show error, retry
```

### UI Screen

**Token Input Screen**
```
┌─────────────────────────────────┐
│  LaCompraGo                     │
├─────────────────────────────────┤
│                                 │
│  Enter your API token:          │
│                                 │
│  ┌───────────────────────────┐  │
│  │ [Paste/type token here]  │  │
│  └───────────────────────────┘  │
│                                 │
│  [Submit]                       │
│                                 │
│  Error: [error message if any]  │
│                                 │
└─────────────────────────────────┘
```

### Acceptance Criteria

- [x] Text field for token input
- [x] Submit button validates token
- [x] Successful validation stores token encrypted
- [x] Failed validation shows error message
- [x] Token persists after app restart
- [x] Clear error handling
- [x] Option to clear token from settings

---

## Feature 2: Product List with Frequency

### User Story
As a user, I want to see a list of products I've purchased with their frequency and last purchase date, so that I know what I buy regularly.

### Requirements

**Functional Requirements**
- Display list of products
- Show frequency (number of times purchased)
- Show last purchase date
- Sort products by frequency (default)
- Store data in JSON file
- Allow manual refresh

**Non-Functional Requirements**
- List should load instantly from local file
- Support for 1000+ products
- Smooth scrolling

### UI Screen

**Product List Screen**
```
┌─────────────────────────────────┐
│  Products      [Refresh] [Cart] │
├─────────────────────────────────┤
│                                 │
│  Milk 1L                        │
│  Frequency: 24 • 3 days ago     │
│  ────────────────────────────   │
│                                 │
│  Bread                          │
│  Frequency: 20 • 1 day ago      │
│  ────────────────────────────   │
│                                 │
│  Eggs (12 pack)                 │
│  Frequency: 12 • 10 days ago    │
│  ────────────────────────────   │
│                                 │
│  ...                            │
│                                 │
└─────────────────────────────────┘
```

### Acceptance Criteria

- [x] Products displayed with name
- [x] Frequency count shown
- [x] Last purchase date shown
- [x] Sorted by frequency (highest first)
- [x] Data loaded from JSON file
- [x] Empty state when no products
- [x] Refresh button to process orders

---

## Feature 3: Order Processing

### User Story
As a user, I want to refresh my product list by processing my orders, so that my frequency data stays up to date.

### Requirements

**Functional Requirements**
- Download orders one by one from API
- Extract products from each order
- Update product frequency and last purchase date
- Track which orders have been processed
- Show progress (current/total)
- Allow cancellation at any time
- Save progress after each order

**Non-Functional Requirements**
- Process orders sequentially (one at a time)
- Update UI with progress
- Handle network errors gracefully
- Preserve partial progress on cancellation

### User Flow

```
1. User taps "Refresh" button
2. App shows processing dialog
3. App fetches list of order IDs
4. App filters out processed orders
5. For each unprocessed order:
   a. Show progress: "Processing order 5 of 20"
   b. Download order details
   c. Extract products
   d. Update frequencies and dates
   e. Save products.json
   f. Mark order as processed
   g. Save processed_orders.json
   h. Update progress display
6. Show completion message
```

### UI Screen

**Processing Dialog**
```
┌─────────────────────────────────┐
│  Processing Orders              │
├─────────────────────────────────┤
│                                 │
│  [Progress Bar]                 │
│                                 │
│  Processing order 5 of 20       │
│  Order ID: order_456            │
│                                 │
│  [Cancel]                       │
│                                 │
└─────────────────────────────────┘
```

**Completion Dialog**
```
┌─────────────────────────────────┐
│  Processing Complete            │
├─────────────────────────────────┤
│                                 │
│  ✓ Processed 20 orders          │
│  ✓ Updated 150 products         │
│                                 │
│  [OK]                           │
│                                 │
└─────────────────────────────────┘
```

**Cancelled Dialog**
```
┌─────────────────────────────────┐
│  Processing Cancelled           │
├─────────────────────────────────┤
│                                 │
│  ℹ Processed 12 of 20 orders    │
│  ℹ Progress has been saved      │
│                                 │
│  [OK]                           │
│                                 │
└─────────────────────────────────┘
```

### Processing Logic

**Update Product Frequency**
```kotlin
// If product exists: increment frequency, update last purchase
// If product is new: create entry with frequency=1

when {
    product exists -> {
        frequency = existing.frequency + 1
        lastPurchase = max(existing.lastPurchase, orderDate)
    }
    product is new -> {
        frequency = 1
        lastPurchase = orderDate
    }
}
```

### Acceptance Criteria

- [x] Refresh button initiates processing
- [x] Orders downloaded sequentially
- [x] Progress shown with count (X of Y)
- [x] Current order ID displayed
- [x] Cancel button stops processing
- [x] Partial progress saved on cancel
- [x] Products.json updated after each order
- [x] Processed orders tracked to avoid duplicates
- [x] Error handling for failed orders (skip and continue)
- [x] Completion message shown
- [x] Product list updated after processing

---

## Feature 4: Shopping Cart Creation

### User Story
As a user, I want to create a shopping cart based on my frequently purchased items, so that I can quickly order my regular products.

### Requirements

**Functional Requirements**
- Automatically select products based on frequency
- Allow manual editing of cart
- Submit cart to API
- Show success/error message

**Non-Functional Requirements**
- Cart generation within 1 second
- Support for 50+ items in cart
- Clear feedback on submission

### User Flow

```
1. User taps "Cart" button
2. App generates cart from top products
3. App shows cart preview
4. User can review items
5. User taps "Submit Cart"
6. App sends cart to API
7. App shows success message
```

### UI Screen

**Cart Preview Screen**
```
┌─────────────────────────────────┐
│  Shopping Cart    [Submit]      │
├─────────────────────────────────┤
│                                 │
│  Auto-selected items (15):      │
│                                 │
│  ☑ Milk 1L          x2          │
│  ☑ Bread            x1          │
│  ☑ Eggs (12 pack)   x1          │
│  ☑ Coffee 250g      x1          │
│  ☑ Butter           x1          │
│  ...                            │
│                                 │
│  Based on your purchase         │
│  frequency                      │
│                                 │
└─────────────────────────────────┘
```

**Submitting Dialog**
```
┌─────────────────────────────────┐
│  Submitting Cart...             │
├─────────────────────────────────┤
│                                 │
│  [Progress Spinner]             │
│                                 │
└─────────────────────────────────┘
```

**Success Dialog**
```
┌─────────────────────────────────┐
│  Cart Submitted                 │
├─────────────────────────────────┤
│                                 │
│  ✓ Cart created successfully    │
│  Cart ID: cart_789              │
│                                 │
│  [OK]                           │
│                                 │
└─────────────────────────────────┘
```

### Cart Generation Logic

**Selection Criteria**
```kotlin
// Select products with highest frequency
// Take top 20 or products with frequency >= threshold

val topProducts = products
    .sortedByDescending { it.frequency }
    .take(20)

// Suggested quantity: 1 or 2 based on frequency
val quantity = when {
    product.frequency >= 20 -> 2
    else -> 1
}
```

### Acceptance Criteria

- [x] Cart generated from frequent products
- [x] Top products selected automatically
- [x] Cart shown for review
- [x] Submit button sends cart to API
- [x] Success message displayed
- [x] Error message on failure
- [x] Option to retry on error

---

## Common UI Elements

### Navigation

Simple screen transitions:
```
Token Input → Product List → Cart Preview
                ↓
           Order Processing
```

### Loading States

**Shimmer/Spinner**
- Show during API calls
- Centered on screen
- With descriptive text

### Empty States

**No Products**
- Message: "No products yet"
- Action: "Refresh to process orders"

**No Token**
- Message: "No API token"
- Action: "Enter token to continue"

### Error States

**Network Error**
- Icon: 📡
- Title: "Connection Error"
- Message: "Please check your internet connection"
- Button: "Retry"

**API Error**
- Icon: ⚠️
- Title: "Error"
- Message: [API error message]
- Button: "OK"

**Token Invalid**
- Icon: 🔑
- Title: "Invalid Token"
- Message: "Please enter a valid API token"
- Button: "OK"

---

## Non-Functional Requirements

### Performance
- Product list load: < 1 second (from file)
- Cart generation: < 1 second
- Order processing: 1-2 seconds per order
- API calls: < 3 seconds

### Usability
- Simple, clear interface
- No animations (keeping it simple)
- Clear feedback for all actions
- Progress indicators for long operations
- Cancellable operations

### Reliability
- Persist data after each order processed
- Handle network interruptions
- Skip problematic orders
- Save partial progress on cancellation

### Security
- Token stored encrypted
- No sensitive data in logs
- HTTPS for all API calls

---

## User Scenarios

### Scenario 1: First Time User

1. Open app
2. See token input screen
3. Paste API token
4. Submit token
5. See empty product list
6. Tap "Refresh"
7. Watch orders being processed
8. See product list populated
9. Tap "Cart"
10. Review and submit cart

### Scenario 2: Returning User

1. Open app
2. Immediately see product list (token stored)
3. View frequencies and dates
4. Optionally tap "Refresh" for updates
5. Create and submit cart

### Scenario 3: Cancelled Processing

1. Tap "Refresh"
2. Processing starts (20 orders)
3. User taps "Cancel" after 12 orders
4. Processing stops
5. Progress saved (12 orders processed)
6. Product list shows updated frequencies
7. Next refresh will process remaining 8 orders

---

## Success Metrics

### User Engagement
- Daily product list views
- Refresh frequency
- Cart submissions

### Performance
- Processing speed per order
- API success rate
- App crash rate < 1%

### Data Quality
- Number of products tracked
- Order processing success rate
- Duplicate order avoidance

---

## Future Enhancements

### Phase 2
- Product categories view
- Search products
- Manual product editing
- Export product list

### Phase 3
- Multiple shopping lists
- Product recommendations
- Price tracking
- Budget monitoring

---

## Conclusion

These simplified feature specifications provide a clear, achievable implementation plan for LaCompraGo, focusing on essential functionality with minimal complexity.
