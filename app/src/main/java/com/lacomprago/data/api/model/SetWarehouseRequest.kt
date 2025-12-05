package com.lacomprago.data.api.model

import com.google.gson.annotations.SerializedName

/**
 * Request body for setting warehouse by postal code.
 *
 * Endpoint: PUT /api/postal-codes/actions/change-pc/
 *
 * @property newPostalCode The postal code to set
 */
data class SetWarehouseRequest(
    @SerializedName("new_postal_code")
    val newPostalCode: String
)

/**
 * Response from setting warehouse by postal code.
 *
 * @property warehouseChanged Whether the warehouse was changed
 */
data class SetWarehouseResponse(
    @SerializedName("warehouse_changed")
    val warehouseChanged: Boolean
)
