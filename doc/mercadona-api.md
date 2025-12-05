# LaCompraGo - Mercadona API Endpoints Reference

## Overview

This document provides a detailed reference of the Mercadona API endpoints used by LaCompraGo. The API endpoints are reverse-engineered from the [mercadona-cli](https://github.com/alfonmga/mercadona-cli) project.

> ⚠️ **WARNING**: These are unofficial API endpoints obtained through reverse engineering. They may change without notice. Use at your own risk.

## Base Configuration

```kotlin
object MercadonaApiConfig {
    const val BASE_URL = "https://tienda.mercadona.es/api/"
    const val CONNECT_TIMEOUT = 30L // seconds
    const val READ_TIMEOUT = 30L // seconds
    const val WRITE_TIMEOUT = 30L // seconds
}
```

## Authentication

### 1. Login / Authenticate

Authenticate with email and password to obtain an access token.

```
POST https://tienda.mercadona.es/api/auth/tokens/
```

**Request Headers**
```
Content-Type: application/json
```

**Request Body**
```json
{
    "username": "<email>",
    "password": "<password>"
}
```

**Response (200 OK)**
```json
{
    "access_token": "<access_token>",
    "customer_id": "<customer_id>"
}
```

**Notes:**
- The `access_token` is used for Bearer authentication in subsequent requests
- The `customer_id` is required for customer-specific API calls
- For LaCompraGo Phase 1, we'll use token paste instead of login

> ⚠️ **Security Note**: Token pasting is used for development and testing purposes.
> For production use, consider implementing proper OAuth flow with secure token
> acquisition. Never share your access token publicly or store it in version control.

---

## Customer Information

### 2. Get Customer Information

Retrieve the authenticated customer's account details.

```
GET https://tienda.mercadona.es/api/customers/{customer_id}/
```

**Request Headers**
```
Authorization: Bearer <access_token>
```

**Path Parameters**
| Parameter | Type | Description |
|-----------|------|-------------|
| customer_id | string | The customer ID obtained from authentication |

**Response (200 OK)**
```json
{
    "cart_id": "<cart_id>",
    "current_postal_code": "<postal_code>",
    "email": "<email>",
    "id": <id>,
    "last_name": "<last_name>",
    "name": "<name>",
    "send_offers": false,
    "uuid": "<uuid>"
}
```

---

## Warehouse Configuration

### 3. Set Warehouse by Postal Code

Change the warehouse based on a postal code.

```
PUT https://tienda.mercadona.es/api/postal-codes/actions/change-pc/
```

**Request Headers**
```
Content-Type: application/json
```

**Request Body**
```json
{
    "new_postal_code": "<postal_code>"
}
```

**Response (200 OK)**
```json
{
    "warehouse_changed": true
}
```

**Response Headers**
```
x-customer-pc: <postal_code>
x-customer-wh: <warehouse_code>
```

---

## Cart Management

### 4. Get Customer Cart

Retrieve the current cart for the customer.

```
GET https://tienda.mercadona.es/api/customers/{customer_id}/cart/
```

**Request Headers**
```
Authorization: Bearer <access_token>
```

**Path Parameters**
| Parameter | Type | Description |
|-----------|------|-------------|
| customer_id | string | The customer ID |

**Response (200 OK)**
```json
{
    "id": "<cart_id>",
    "lines": [],
    "open_order_id": <open_order_id>,
    "products_count": 0,
    "summary": {
        "total": "0.00"
    },
    "version": 3
}
```

**Cart with Products Response**
```json
{
    "id": "<cart_id>",
    "lines": [
        {
            "product": {
                "badges": {
                    "is_water": false,
                    "requires_age_check": false
                },
                "categories": [
                    {
                        "id": 4,
                        "level": 0,
                        "name": "Charcutería y quesos",
                        "order": 159
                    }
                ],
                "display_name": "Queso camembert Marcillat",
                "id": "51621",
                "limit": 999,
                "packaging": "Caja",
                "price_instructions": {
                    "approx_size": false,
                    "bulk_price": "5.21",
                    "bunch_selector": false,
                    "drained_weight": null,
                    "increment_bunch_amount": 1.0,
                    "is_new": false,
                    "is_pack": false,
                    "iva": 4,
                    "min_bunch_amount": 1.0,
                    "pack_size": null,
                    "price_decreased": false,
                    "reference_format": "kg",
                    "reference_price": "5.21",
                    "selling_method": 0,
                    "size_format": "kg",
                    "total_units": null,
                    "unit_name": null,
                    "unit_price": "1.25",
                    "unit_selector": true,
                    "unit_size": 0.24
                },
                "published": true,
                "share_url": "https://tienda.mercadona.es/product/51621/queso-camembert-marcillat-caja",
                "slug": "queso-camembert-marcillat-caja",
                "thumbnail": "https://prod-mercadona.imgix.net/images/..."
            },
            "quantity": 6.0,
            "sources": ["+RP"],
            "version": 5
        }
    ],
    "open_order_id": null,
    "products_count": 1,
    "summary": {
        "total": "7.50"
    },
    "version": 5
}
```

### 5. Add Products to Cart

Add or update products in the cart.

```
PUT https://tienda.mercadona.es/api/customers/{customer_id}/cart/
```

**Request Headers**
```
Authorization: Bearer <access_token>
Content-Type: application/json
```

**Path Parameters**
| Parameter | Type | Description |
|-----------|------|-------------|
| customer_id | string | The customer ID |

**Request Body**
```json
{
    "id": "<cart_id>",
    "version": 1,
    "lines": [
        {
            "quantity": 6,
            "product_id": "51621",
            "sources": []
        }
    ]
}
```

**Response (200 OK)**
Returns the updated cart (same structure as Get Cart response).

**Important Notes:**
- The `version` must match the current cart version
- Sending an empty `lines` array clears the cart
- Each request replaces the entire cart contents

---

## Order Management

### 6. List All Orders

Get a paginated list of all customer orders.

```
GET https://tienda.mercadona.es/api/customers/{customer_id}/orders/?page={page_num}
```

**Request Headers**
```
Authorization: Bearer <access_token>
```

**Path Parameters**
| Parameter | Type | Description |
|-----------|------|-------------|
| customer_id | string | The customer ID |

**Query Parameters**
| Parameter | Type | Description |
|-----------|------|-------------|
| page | integer | Page number for pagination (optional, defaults to 1) |

**Response (200 OK)**
```json
{
    "next_page": null,
    "results": [
        {
            "address": {
                "address": "<address>",
                "address_detail": "<address_detail>",
                "comments": "<comments>",
                "entered_manually": false,
                "id": <address_id>,
                "latitude": "<latitude>",
                "longitude": "<longitude>",
                "permanent_address": true,
                "postal_code": "<postal_code>",
                "town": "Madrid"
            },
            "changes_until": "2021-04-29T17:59:59Z",
            "click_and_collect": false,
            "customer_phone": "<customer_phone>",
            "end_date": "2021-04-30T16:00:00Z",
            "final_price": false,
            "id": 8312430,
            "last_edit_message": "Pedido editado hace 16 horas.",
            "order_id": 8312430,
            "payment_method": {
                "credit_card_number": "<last_4_digits>",
                "credit_card_type": 1,
                "default_card": true,
                "expiration_status": "valid",
                "expires_month": "<expires_month>",
                "expires_year": "<expires_year>",
                "id": <payment_method_id>
            },
            "payment_status": 0,
            "phone_country_code": "34",
            "phone_national_number": "<phone_national_number>",
            "price": "65.94",
            "products_count": 28,
            "service_rating_token": null,
            "slot": {
                "available": true,
                "end": "2021-04-30T16:00:00Z",
                "id": <slot_id>,
                "price": "7.21",
                "start": "2021-04-30T15:00:00Z"
            },
            "slot_size": 1,
            "start_date": "2021-04-30T15:00:00Z",
            "status": 2,
            "status_ui": "confirmed",
            "summary": {
                "products": "65.94",
                "slot": "7.21",
                "tax_base": "67.07",
                "taxes": "6.08",
                "total": "73.15",
                "volume_extra_cost": {
                    "cost_by_extra_liter": "0.1",
                    "threshold": 70,
                    "total": "0.00",
                    "total_extra_liters": 0.0
                }
            },
            "warehouse_code": "mad1"
        }
    ]
}
```

**Order Status Values**
| Status | Status UI | Description |
|--------|-----------|-------------|
| 0 | pending | Order is pending |
| 1 | processing | Order is being processed |
| 2 | confirmed | Order is confirmed |
| 3 | delivered | Order has been delivered |
| 4 | cancelled | Order was cancelled |

### 7. Get Order Details

Get detailed information about a specific order.

```
GET https://tienda.mercadona.es/api/customers/{customer_id}/orders/{order_id}/
```

**Request Headers**
```
Authorization: Bearer <access_token>
```

**Path Parameters**
| Parameter | Type | Description |
|-----------|------|-------------|
| customer_id | string | The customer ID |
| order_id | integer | The order ID |

**Response (200 OK)**
Same structure as a single order in the List Orders response.

---

## Recommendations

### 8. Get My Recommended Products

Get personalized product recommendations based on purchase history.

```
GET https://tienda.mercadona.es/api/customers/{customer_id}/recommendations/myregulars/{regular_type}/
```

**Request Headers**
```
Authorization: Bearer <access_token>
```

**Path Parameters**
| Parameter | Type | Description |
|-----------|------|-------------|
| customer_id | string | The customer ID |
| regular_type | string | Type of recommendations: `precision` (what I most buy) or `recall` (I also buy) |

**Response (200 OK)**
```json
{
    "next_page": null,
    "results": [
        {
            "product": {
                "badges": {
                    "is_water": false,
                    "requires_age_check": false
                },
                "categories": [
                    {
                        "id": 5,
                        "level": 0,
                        "name": "Panadería y pastelería",
                        "order": 508
                    }
                ],
                "display_name": "Empanada de verduras",
                "id": "84780",
                "limit": 999,
                "packaging": "Pieza",
                "price_instructions": {
                    "approx_size": false,
                    "bulk_price": "5.90",
                    "bunch_selector": false,
                    "drained_weight": null,
                    "increment_bunch_amount": 1.0,
                    "is_new": false,
                    "is_pack": false,
                    "iva": 10,
                    "min_bunch_amount": 1.0,
                    "pack_size": null,
                    "price_decreased": false,
                    "reference_format": "kg",
                    "reference_price": "5.90",
                    "selling_method": 0,
                    "size_format": "kg",
                    "total_units": null,
                    "unit_name": null,
                    "unit_price": "2.95",
                    "unit_selector": true,
                    "unit_size": 0.5
                },
                "published": true,
                "share_url": "https://tienda.mercadona.es/product/84780/...",
                "slug": "empanada-verduras-apto-veganos-pieza",
                "thumbnail": "https://prod-mercadona.imgix.net/images/..."
            },
            "recommended_quantity": 1,
            "selling_method": 0
        }
    ]
}
```

---

## Checkout Flow

### 9. Create Checkout

Initiate a checkout from the current cart.

```
POST https://tienda.mercadona.es/api/customers/{customer_id}/checkouts/
```

**Request Headers**
```
Authorization: Bearer <access_token>
Content-Type: application/json
```

**Path Parameters**
| Parameter | Type | Description |
|-----------|------|-------------|
| customer_id | string | The customer ID |

**Request Body**
```json
{
    "cart": {
        "id": "<cart_id>",
        "version": 25,
        "lines": [
            {
                "quantity": 1,
                "version": 25,
                "product_id": "66463",
                "sources": []
            }
        ]
    }
}
```

**Response (201 Created)**
```json
{
    "address": {
        "address": "<address>",
        "address_detail": "<address_detail>",
        "comments": "<comments>",
        "entered_manually": false,
        "id": <id>,
        "latitude": "<lat>",
        "longitude": "<long>",
        "permanent_address": true,
        "postal_code": "<postal_code>",
        "town": "Madrid"
    },
    "authentication_type": "sca",
    "cart": {
        "id": "<cart_id>",
        "lines": [...]
    },
    "click_and_collect": false,
    "click_and_collect_available": false,
    "customer_phone": "+34...",
    "id": <checkout_id>,
    "order_id": null,
    "payment_method": {
        "credit_card_number": "****",
        "credit_card_type": 1,
        "default_card": true,
        "expiration_status": "valid",
        "expires_month": "<month>",
        "expires_year": "<year>",
        "id": <id>
    },
    "phone_country_code": "34",
    "phone_national_number": "<phone>",
    "price": "0.92",
    "requires_address_confirmation": false,
    "requires_age_check": true,
    "slot": null,
    "slot_size": 1,
    "summary": {
        "products": "0.92",
        "slot": "0.00",
        "tax_base": "0.76",
        "taxes": "0.16",
        "total": "0.92",
        "volume_extra_cost": {
            "cost_by_extra_liter": "0.1",
            "threshold": 70,
            "total": "0.00",
            "total_extra_liters": 0.0
        }
    }
}
```

### 10. List Address Delivery Slots

Get available delivery time slots for an address.

```
GET https://tienda.mercadona.es/api/customers/{customer_id}/addresses/{address_id}/slots/
```

**Request Headers**
```
Authorization: Bearer <access_token>
```

**Path Parameters**
| Parameter | Type | Description |
|-----------|------|-------------|
| customer_id | string | The customer ID |
| address_id | integer | The address ID |

**Response (200 OK)**
```json
{
    "next_page": null,
    "results": [
        {
            "available": true,
            "end": "2021-05-05T07:00:00Z",
            "id": "125677",
            "open": true,
            "price": "7.21",
            "start": "2021-05-05T06:00:00Z"
        },
        {
            "available": false,
            "end": "2021-05-05T08:00:00Z",
            "id": "125676",
            "open": true,
            "price": "7.21",
            "start": "2021-05-05T07:00:00Z"
        }
    ]
}
```

### 11. Set Checkout Delivery Info

Set the delivery address and time slot for a checkout.

```
PUT https://tienda.mercadona.es/api/customers/{customer_id}/checkouts/{checkout_id}/delivery-info/
```

**Request Headers**
```
Authorization: Bearer <access_token>
Content-Type: application/json
```

**Path Parameters**
| Parameter | Type | Description |
|-----------|------|-------------|
| customer_id | string | The customer ID |
| checkout_id | integer | The checkout ID |

**Request Body**
```json
{
    "address": {
        "id": <address_id>
    },
    "slot": {
        "id": "<slot_id>"
    }
}
```

**Response (200 OK)**
Returns the updated checkout object.

### 12. Submit Checkout Order

Finalize and submit the checkout as an order.

```
POST https://tienda.mercadona.es/api/customers/{customer_id}/checkouts/{checkout_id}/orders/
```

**Request Headers**
```
Authorization: Bearer <access_token>
```

**Path Parameters**
| Parameter | Type | Description |
|-----------|------|-------------|
| customer_id | string | The customer ID |
| checkout_id | integer | The checkout ID |

**Response (201 Created)**
Returns the created order object.

---

## Error Responses

### Common Error Codes

| HTTP Code | Description | Action |
|-----------|-------------|--------|
| 400 | Bad Request | Validate request parameters |
| 401 | Unauthorized | Token invalid or expired - re-authenticate |
| 403 | Forbidden | Access denied - check permissions |
| 404 | Not Found | Resource doesn't exist |
| 429 | Too Many Requests | Rate limited - wait and retry |
| 500 | Server Error | Retry with exponential backoff |
| 503 | Service Unavailable | Retry later |

### Error Response Format

```json
{
    "error": "Error message description"
}
```

---

## Rate Limiting

Based on observed behavior:
- Recommended: Maximum 60 requests per minute
- Add delay between sequential order fetches (1-2 seconds)
- Implement exponential backoff for retries

---

## Security Headers

All requests should include:
```
User-Agent: LaCompraGo/1.0 (Android)
Content-Type: application/json (for POST/PUT requests)
Authorization: Bearer <access_token> (for authenticated endpoints)
```

---

## Endpoint Summary for LaCompraGo

### Phase 1 - Token Validation (Current Priority)
| # | Endpoint | Method | Purpose | Priority |
|---|----------|--------|---------|----------|
| 2 | `/api/customers/{customer_id}/` | GET | Validate token / Get customer info | HIGH |

### Phase 2 - Order Processing
| # | Endpoint | Method | Purpose | Priority |
|---|----------|--------|---------|----------|
| 6 | `/api/customers/{customer_id}/orders/` | GET | List all orders | HIGH |
| 7 | `/api/customers/{customer_id}/orders/{order_id}/` | GET | Get order details | HIGH |

### Phase 3 - Recommendations & Cart
| # | Endpoint | Method | Purpose | Priority |
|---|----------|--------|---------|----------|
| 8 | `/api/customers/{customer_id}/recommendations/myregulars/{type}/` | GET | Get product recommendations | MEDIUM |
| 4 | `/api/customers/{customer_id}/cart/` | GET | Get current cart | MEDIUM |
| 5 | `/api/customers/{customer_id}/cart/` | PUT | Update cart | MEDIUM |

### Phase 4 - Future (Not in scope yet)
| # | Endpoint | Method | Purpose | Priority |
|---|----------|--------|---------|----------|
| 1 | `/api/auth/tokens/` | POST | Login with credentials | LOW |
| 9-12 | Checkout endpoints | Various | Complete checkout flow | LOW |

---

## Data Models for Implementation

### Required Model Changes

The current implementation uses placeholder models. These need to be updated to match the actual Mercadona API responses.

See [data-models.md](./data-models.md) for the updated model definitions.

---

## References

- [mercadona-cli GitHub Repository](https://github.com/alfonmga/mercadona-cli)
- [Mercadona Online Store](https://tienda.mercadona.es)
