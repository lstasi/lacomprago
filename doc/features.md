# LaComprago - Feature Specifications

## Overview

This document provides detailed specifications for each feature of the LaComprago application, including user stories, acceptance criteria, and technical requirements.

## Feature 1: OAuth Authentication

### User Story
As a user, I want to securely authenticate with my supermarket account using OAuth, so that I can access my order history and create shopping carts.

### Requirements

**Functional Requirements**
- User can initiate login from the app
- Login opens browser for OAuth authentication
- App handles OAuth callback and stores token securely
- App automatically refreshes expired tokens
- User can logout and clear session

**Non-Functional Requirements**
- Authentication must complete within 30 seconds
- Tokens must be stored encrypted
- Session persists across app restarts
- No sensitive data in logs

### User Flow

```
1. User opens app
2. App checks for valid token
   ├─ Token valid → Navigate to Home
   └─ Token invalid → Show Login Screen
3. User taps "Login" button
4. Browser opens with OAuth page
5. User enters credentials
6. User grants permissions
7. Browser redirects back to app
8. App exchanges code for token
9. App stores token securely
10. App navigates to Home screen
```

### UI Screens

**Login Screen**
- App logo
- Welcome message
- "Login with OAuth" button
- Privacy policy link
- Terms of service link

**Loading Screen**
- Progress indicator
- "Authenticating..." message
- Cancel button

### Acceptance Criteria

- [ ] Login button initiates OAuth flow
- [ ] OAuth opens in Chrome Custom Tabs
- [ ] Successful login stores token
- [ ] Failed login shows error message
- [ ] Token persists after app restart
- [ ] Expired tokens refresh automatically
- [ ] Logout clears all session data
- [ ] Network errors handled gracefully

---

## Feature 2: Order History

### User Story
As a user, I want to view my past orders, so that I can see what I have purchased previously.

### Requirements

**Functional Requirements**
- Display list of past orders
- Show order date, total amount, and item count
- Allow filtering by date range
- Support pull-to-refresh
- Implement pagination for large order lists
- Cache orders for offline viewing
- Show order details on tap

**Non-Functional Requirements**
- List should load within 3 seconds
- Smooth scrolling performance
- Support for 1000+ orders
- Offline access to cached data

### User Flow

```
1. User navigates to Order History tab
2. App fetches orders from API
3. App displays order list
4. User scrolls through orders
5. User taps on an order
6. App shows order details
7. User can return to list
```

### UI Screens

**Order List Screen**
```
┌─────────────────────────────────┐
│  Order History        🔄 Filter  │
├─────────────────────────────────┤
│                                 │
│  📦 Oct 20, 2024                │
│     €125.50 • 15 items          │
│     SuperMarket Downtown         │
│                                 │
│  📦 Oct 15, 2024                │
│     €89.30 • 10 items           │
│     SuperMarket Central          │
│                                 │
│  📦 Oct 10, 2024                │
│     €156.80 • 20 items          │
│     SuperMarket Downtown         │
│                                 │
│  [Load More]                    │
│                                 │
└─────────────────────────────────┘
```

**Order Details Screen**
```
┌─────────────────────────────────┐
│  ← Order #ORD-2024-001          │
├─────────────────────────────────┤
│  Date: Oct 20, 2024 15:30       │
│  Store: SuperMarket Downtown    │
│  Status: Completed              │
│  Total: €125.50                 │
├─────────────────────────────────┤
│  Items (15)                     │
│                                 │
│  Milk 1L                        │
│  2x €1.99 = €3.98               │
│  Dairy • Local Farm             │
│                                 │
│  Bread                          │
│  1x €2.50 = €2.50               │
│  Bakery • House Brand           │
│                                 │
│  ...                            │
│                                 │
└─────────────────────────────────┘
```

### Data Requirements

**Order Information**
- Order ID and number
- Date and time
- Total amount and currency
- Status
- Store information
- List of items

**Order Item Information**
- Product name
- Quantity and unit
- Price per unit
- Total price
- Category and brand

### Acceptance Criteria

- [ ] Orders displayed in reverse chronological order
- [ ] Each order shows date, total, and item count
- [ ] Pull-to-refresh updates order list
- [ ] Pagination loads more orders
- [ ] Tap order opens detail view
- [ ] Order details show all items
- [ ] Offline access to cached orders
- [ ] Loading states properly displayed
- [ ] Empty state when no orders
- [ ] Error states handled gracefully

---

## Feature 3: Product Statistics

### User Story
As a user, I want to see statistics about my purchases, so that I can understand my shopping patterns and make informed decisions.

### Requirements

**Functional Requirements**
- Calculate purchase frequency for each product
- Track last purchase date
- Show average quantity per order
- Display total quantity purchased
- Calculate average price paid
- Estimate purchase interval
- Categorize products by frequency
- Sort and filter statistics

**Non-Functional Requirements**
- Statistics calculation within 5 seconds
- Support for 1000+ unique products
- Update statistics in background
- Cache calculated statistics

### User Flow

```
1. User navigates to Statistics tab
2. App calculates product statistics
3. App displays product list with stats
4. User can sort by frequency/date/name
5. User can filter by category
6. User taps product for details
7. App shows detailed statistics
```

### UI Screens

**Statistics List Screen**
```
┌─────────────────────────────────┐
│  Statistics      🔽 Sort ⚙️ Filter│
├─────────────────────────────────┤
│                                 │
│  🔥 ESSENTIAL                   │
│                                 │
│  Milk 1L                        │
│  Purchased 24 times             │
│  Last: 3 days ago               │
│  Avg: Every 5 days              │
│  ⭐⭐⭐⭐⭐                        │
│                                 │
│  Bread                          │
│  Purchased 20 times             │
│  Last: 1 day ago                │
│  Avg: Every 4 days              │
│  ⭐⭐⭐⭐⭐                        │
│                                 │
│  📊 REGULAR                     │
│                                 │
│  Eggs                           │
│  Purchased 12 times             │
│  Last: 10 days ago              │
│  Avg: Every 14 days             │
│  ⭐⭐⭐⭐                          │
│                                 │
└─────────────────────────────────┘
```

**Product Statistics Detail Screen**
```
┌─────────────────────────────────┐
│  ← Milk 1L                      │
├─────────────────────────────────┤
│  📊 Purchase Statistics         │
│                                 │
│  Total Purchases: 24            │
│  Total Quantity: 48 liters      │
│  Avg Quantity: 2 per order      │
│                                 │
│  First Purchased: 6 months ago  │
│  Last Purchased: 3 days ago     │
│  Purchase Interval: Every 5 days│
│                                 │
│  💰 Price Statistics            │
│                                 │
│  Average Price: €1.99           │
│  Lowest Price: €1.79            │
│  Highest Price: €2.19           │
│                                 │
│  📦 Product Info                │
│                                 │
│  Category: Dairy                │
│  Brand: Local Farm              │
│  Unit: liter                    │
│                                 │
│  🎯 Recommendation: ESSENTIAL   │
│  "You buy this very regularly"  │
│                                 │
└─────────────────────────────────┘
```

### Statistics Calculations

**Purchase Frequency**
```
frequency = total_number_of_purchases
```

**Purchase Interval**
```
interval_days = (last_purchase_date - first_purchase_date) / (frequency - 1)
```

**Average Quantity per Order**
```
avg_quantity = total_quantity_purchased / frequency
```

**Frequency Score (0.0 to 1.0)**
```
// Normalize based on maximum frequency
frequency_score = product_frequency / max_product_frequency
```

**Recommendation Level**
```
if frequency_score >= 0.7 → ESSENTIAL
if frequency_score >= 0.4 → REGULAR
if frequency_score >= 0.2 → OCCASIONAL
else → RARE
```

### Sorting Options

- **Frequency (High to Low)**: Most purchased first
- **Frequency (Low to High)**: Least purchased first
- **Last Purchase (Recent)**: Recently bought first
- **Last Purchase (Oldest)**: Longest time since purchase
- **Name (A-Z)**: Alphabetical
- **Category**: Grouped by category

### Filtering Options

- **By Category**: Dairy, Bakery, Produce, etc.
- **By Recommendation Level**: Essential, Regular, Occasional, Rare
- **By Date Range**: Products bought within specific period
- **By Frequency**: Minimum purchase count

### Acceptance Criteria

- [ ] Statistics calculated from order history
- [ ] Each product shows frequency and last purchase
- [ ] Purchase interval displayed when applicable
- [ ] Products sorted by frequency by default
- [ ] User can change sort order
- [ ] User can filter by category
- [ ] Tap product shows detailed statistics
- [ ] Recommendation levels displayed correctly
- [ ] Empty state when no purchase history
- [ ] Statistics update after new orders sync

---

## Feature 4: Shopping Cart Builder

### User Story
As a user, I want to automatically create a shopping cart based on my purchase patterns, so that I can quickly order my regular items without manual selection.

### Requirements

**Functional Requirements**
- Auto-generate cart from statistics
- Include essential and regular items
- Calculate suggested quantities
- Allow manual editing of cart
- Add/remove items manually
- Adjust quantities
- Submit cart to API
- Show cart submission status
- Save draft carts

**Non-Functional Requirements**
- Cart generation within 3 seconds
- Support for 100+ items in cart
- Optimistic UI updates
- Retry failed submissions

### User Flow

```
1. User navigates to Shopping Cart tab
2. User taps "Generate Cart" button
3. App analyzes purchase patterns
4. App creates cart with recommended items
5. App displays cart for review
6. User reviews and edits cart
7. User taps "Submit Cart" button
8. App submits cart to API
9. App shows confirmation
```

### UI Screens

**Cart Builder Screen (Empty)**
```
┌─────────────────────────────────┐
│  Shopping Cart                  │
├─────────────────────────────────┤
│                                 │
│                                 │
│           🛒                    │
│                                 │
│     No items in cart            │
│                                 │
│  [🤖 Generate Smart Cart]       │
│  [➕ Add Items Manually]        │
│                                 │
│                                 │
└─────────────────────────────────┘
```

**Cart Builder Screen (With Items)**
```
┌─────────────────────────────────┐
│  Shopping Cart      🤖 Regenerate│
├─────────────────────────────────┤
│  15 items • Est. €125.50        │
│                                 │
│  🔥 Essential Items (5)         │
│                                 │
│  ☑ Milk 1L                      │
│     [- 2 +]  €3.98  🗑️         │
│     Last bought 3 days ago      │
│                                 │
│  ☑ Bread                        │
│     [- 1 +]  €2.50  🗑️         │
│     Last bought 1 day ago       │
│                                 │
│  📊 Regular Items (10)          │
│                                 │
│  ☑ Eggs (12 pack)               │
│     [- 1 +]  €4.50  🗑️         │
│     Last bought 10 days ago     │
│                                 │
│  ...                            │
│                                 │
│  [➕ Add More Items]            │
│                                 │
│  [📤 Submit Cart]               │
│                                 │
└─────────────────────────────────┘
```

**Cart Submission Screen**
```
┌─────────────────────────────────┐
│  ← Cart Submitted               │
├─────────────────────────────────┤
│                                 │
│           ✅                     │
│                                 │
│  Cart Successfully Submitted!   │
│                                 │
│  Cart ID: #CART-2024-001        │
│  15 items • €125.50             │
│                                 │
│  Your order is being processed  │
│  by the supermarket.            │
│                                 │
│  [View Cart Status]             │
│  [Create New Cart]              │
│                                 │
└─────────────────────────────────┘
```

### Cart Generation Algorithm

**Step 1: Select Products**
```kotlin
fun selectProductsForCart(
    statistics: List<ProductStatistics>
): List<Product> {
    val products = mutableListOf<Product>()
    
    // Add all essential products (high frequency)
    products.addAll(
        statistics.filter { it.recommendationLevel == ESSENTIAL }
            .map { it.product }
    )
    
    // Add regular products if last purchase was > interval
    val regularProducts = statistics.filter { 
        it.recommendationLevel == REGULAR &&
        daysSinceLastPurchase(it) > it.purchaseIntervalDays
    }
    products.addAll(regularProducts.map { it.product })
    
    return products
}
```

**Step 2: Calculate Quantities**
```kotlin
fun calculateQuantity(statistics: ProductStatistics): Int {
    // Use average quantity, rounded up
    return ceil(statistics.averageQuantityPerOrder).toInt()
        .coerceAtLeast(1)
}
```

**Step 3: Estimate Prices**
```kotlin
fun estimatePrice(product: Product, quantity: Int): Double {
    val price = product.currentPrice ?: product.averagePrice
    return price * quantity
}
```

### Manual Editing Features

**Add Item**
- Search products from order history
- Select product
- Set quantity
- Add to cart

**Remove Item**
- Tap delete icon
- Confirm deletion
- Update totals

**Adjust Quantity**
- Use +/- buttons
- Enter quantity manually
- Update item total
- Update cart total

**Toggle Selection**
- Check/uncheck item
- Keep in cart but exclude from submission
- Visual indication of excluded items

### Cart Validation

**Before Submission**
- At least one item selected
- All quantities > 0
- All products valid
- User has network connection

### Acceptance Criteria

- [ ] Generate cart button creates smart cart
- [ ] Cart includes essential products
- [ ] Cart includes regular products due for purchase
- [ ] Suggested quantities based on history
- [ ] User can add items manually
- [ ] User can remove items
- [ ] User can adjust quantities
- [ ] User can toggle item selection
- [ ] Cart shows estimated total
- [ ] Submit cart sends to API
- [ ] Success confirmation displayed
- [ ] Failed submission allows retry
- [ ] Draft carts can be saved
- [ ] Cart persists across app restarts

---

## Feature 5: Data Synchronization

### User Story
As a user, I want my order history to stay up-to-date automatically, so that I always have the latest information.

### Requirements

**Functional Requirements**
- Sync orders on app launch
- Sync orders on pull-to-refresh
- Background sync (optional)
- Incremental sync (only new orders)
- Conflict resolution
- Sync status indication

**Non-Functional Requirements**
- Sync completes within 10 seconds for 100 orders
- Minimal battery impact
- Minimal data usage
- Works on poor network

### Sync Strategy

**Initial Sync**
- Fetch all orders from API
- Store in local database
- Calculate statistics

**Incremental Sync**
- Fetch orders since last sync
- Merge with local data
- Update statistics

**Conflict Resolution**
- Server data always wins
- Local changes overwritten
- No local modifications expected for orders

### Acceptance Criteria

- [ ] Orders sync on app launch
- [ ] Pull-to-refresh triggers sync
- [ ] Only new orders fetched incrementally
- [ ] Sync status shown to user
- [ ] Failed sync can be retried
- [ ] Works with poor network connection
- [ ] Minimal battery drain
- [ ] Statistics updated after sync

---

## Common UI Elements

### Navigation

**Bottom Navigation Bar**
```
┌─────────────────────────────────┐
│  📋 Orders  │  📊 Stats  │  🛒 Cart │
└─────────────────────────────────┘
```

### Loading States

**Shimmer Loading**
- Used for list items
- Shows expected layout
- Animated shimmer effect

**Spinner Loading**
- Used for actions
- Centered on screen
- With descriptive text

### Empty States

**No Orders**
- Icon: 📭
- Title: "No Orders Yet"
- Message: "Your order history will appear here"

**No Statistics**
- Icon: 📊
- Title: "No Statistics Available"
- Message: "Purchase some items to see statistics"

**Empty Cart**
- Icon: 🛒
- Title: "Cart is Empty"
- Message: "Generate a smart cart or add items manually"

### Error States

**Network Error**
- Icon: 📡
- Title: "No Internet Connection"
- Message: "Please check your connection"
- Button: "Retry"

**Server Error**
- Icon: ⚠️
- Title: "Something Went Wrong"
- Message: "Please try again later"
- Button: "Retry"

**Authentication Error**
- Icon: 🔒
- Title: "Session Expired"
- Message: "Please login again"
- Button: "Login"

---

## Non-Functional Requirements

### Performance
- App launch: < 2 seconds
- Screen navigation: < 500ms
- API calls: < 3 seconds
- Statistics calculation: < 5 seconds
- Cart generation: < 3 seconds

### Accessibility
- Support TalkBack
- Minimum touch target: 48dp
- Color contrast ratio: 4.5:1
- Proper content descriptions

### Localization
- Support for multiple languages (future)
- Date/time formatting
- Currency formatting
- Number formatting

### Security
- Encrypted token storage
- HTTPS only
- No sensitive data in logs
- Secure data handling

### Compatibility
- Android 14 (API 34) minimum
- No backward compatibility needed
- Support different screen sizes
- Support portrait and landscape

---

## Success Metrics

### User Engagement
- Daily active users
- Session duration
- Feature usage rates
- Cart generation usage
- Cart submission rate

### Performance Metrics
- App crash rate < 1%
- API success rate > 99%
- Average response time < 3s
- App load time < 2s

### Business Metrics
- User retention rate
- Cart submission rate
- Average items per cart
- Time to create cart

---

## Future Enhancements

### Phase 2
- Product recommendations
- Price alerts
- Shopping lists
- Meal planning integration

### Phase 3
- Family account sharing
- Budget tracking
- Nutritional analysis
- Recipe suggestions

### Phase 4
- Voice commands
- Widgets
- Wear OS support
- Push notifications

---

## Conclusion

These feature specifications provide a complete blueprint for implementing the LaComprago application, ensuring all requirements are clearly defined, measurable, and achievable.
