package com.lacomprago.data.api.debug

/**
 * HTTP methods supported by the debug API tester.
 */
enum class HttpMethod {
    GET, POST, PUT, DELETE
}

/**
 * Definition of an API endpoint for debug testing.
 *
 * @property id Unique identifier for the endpoint
 * @property name Human-readable name
 * @property method HTTP method
 * @property path API path pattern (with {placeholders} for path parameters)
 * @property pathParams List of path parameter names
 * @property queryParams List of query parameter names
 * @property hasBody Whether the request has a body
 * @property description Description of what the endpoint does
 * @property sampleBody Sample request body for POST/PUT requests
 */
data class ApiEndpoint(
    val id: String,
    val name: String,
    val method: HttpMethod,
    val path: String,
    val pathParams: List<String> = emptyList(),
    val queryParams: List<String> = emptyList(),
    val hasBody: Boolean = false,
    val description: String,
    val sampleBody: String? = null
)

/**
 * Definitions of all available Mercadona API endpoints for debug testing.
 */
object EndpointDefinitions {
    val endpoints = listOf(
        ApiEndpoint(
            id = "customer_info",
            name = "Customer Info",
            method = HttpMethod.GET,
            path = "customers/{customer_id}/",
            pathParams = listOf("customer_id"),
            description = "Get customer information and validate token"
        ),
        ApiEndpoint(
            id = "get_cart",
            name = "Get Cart",
            method = HttpMethod.GET,
            path = "customers/{customer_id}/cart/",
            pathParams = listOf("customer_id"),
            description = "Get current shopping cart"
        ),
        ApiEndpoint(
            id = "list_orders",
            name = "List Orders",
            method = HttpMethod.GET,
            path = "customers/{customer_id}/orders/",
            pathParams = listOf("customer_id"),
            queryParams = listOf("page"),
            description = "Get paginated list of orders"
        ),
        ApiEndpoint(
            id = "get_order",
            name = "Get Order Details",
            method = HttpMethod.GET,
            path = "customers/{customer_id}/orders/{order_id}/",
            pathParams = listOf("customer_id", "order_id"),
            description = "Get details of a specific order"
        ),
        ApiEndpoint(
            id = "get_recommendations_precision",
            name = "Recommendations (Precision)",
            method = HttpMethod.GET,
            path = "customers/{customer_id}/recommendations/myregulars/precision/",
            pathParams = listOf("customer_id"),
            description = "Get product recommendations - What I most buy"
        ),
        ApiEndpoint(
            id = "get_recommendations_recall",
            name = "Recommendations (Recall)",
            method = HttpMethod.GET,
            path = "customers/{customer_id}/recommendations/myregulars/recall/",
            pathParams = listOf("customer_id"),
            description = "Get product recommendations - I also buy"
        ),
        ApiEndpoint(
            id = "set_warehouse",
            name = "Set Warehouse",
            method = HttpMethod.PUT,
            path = "postal-codes/actions/change-pc/",
            hasBody = true,
            description = "Change warehouse by postal code",
            sampleBody = """{"new_postal_code": "28001"}"""
        ),
        ApiEndpoint(
            id = "update_cart",
            name = "Update Cart",
            method = HttpMethod.PUT,
            path = "customers/{customer_id}/cart/",
            pathParams = listOf("customer_id"),
            hasBody = true,
            description = "Update shopping cart with products",
            sampleBody = """{
  "id": "cart_id_here",
  "version": 1,
  "lines": [
    {
      "product_id": "12345",
      "quantity": 1,
      "sources": []
    }
  ]
}"""
        )
    )

    /**
     * Find an endpoint by its ID.
     */
    fun findById(id: String): ApiEndpoint? = endpoints.find { it.id == id }
}
