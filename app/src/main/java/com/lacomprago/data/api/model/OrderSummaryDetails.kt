package com.lacomprago.data.api.model

import com.google.gson.annotations.SerializedName

/**
 * Order summary with totals, taxes, and additional costs.
 *
 * Based on actual API response structure (see fixtures/orders.json).
 *
 * @property products Total price of products
 * @property slot Delivery slot price
 * @property slotBonus Slot bonus/discount, if any
 * @property total Total order amount
 * @property taxes Tax amount
 * @property taxType Type of tax (e.g., "iva")
 * @property taxBase Tax base amount
 * @property volumeExtraCost Additional cost for extra volume
 */
data class OrderSummaryDetails(
    val products: String,
    val slot: String,
    @SerializedName("slot_bonus")
    val slotBonus: String?,
    val total: String,
    val taxes: String,
    @SerializedName("tax_type")
    val taxType: String,
    @SerializedName("tax_base")
    val taxBase: String,
    @SerializedName("volume_extra_cost")
    val volumeExtraCost: VolumeExtraCost
)

/**
 * Extra cost for volume exceeding threshold.
 *
 * @property threshold Liters threshold before extra cost applies
 * @property costByExtraLiter Cost per extra liter
 * @property totalExtraLiters Total extra liters in the order
 * @property total Total extra cost
 */
data class VolumeExtraCost(
    val threshold: Int,
    @SerializedName("cost_by_extra_liter")
    val costByExtraLiter: String,
    @SerializedName("total_extra_liters")
    val totalExtraLiters: Double,
    val total: String
)
