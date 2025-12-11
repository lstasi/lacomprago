package com.lacomprago.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lacomprago.R
import com.lacomprago.data.api.model.RecommendationItem
import com.lacomprago.databinding.ItemRecommendationBinding
import com.lacomprago.model.Product

/**
 * Adapter for displaying recommendation items in a RecyclerView.
 *
 * @property localProducts List of local products for comparison
 */
class RecommendationAdapter(
    private val localProducts: List<Product>
) : ListAdapter<RecommendationItem, RecommendationAdapter.RecommendationViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendationViewHolder {
        val binding = ItemRecommendationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RecommendationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecommendationViewHolder, position: Int) {
        holder.bind(getItem(position), localProducts)
    }

    class RecommendationViewHolder(
        private val binding: ItemRecommendationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RecommendationItem, localProducts: List<Product>) {
            val product = item.product

            // Product name
            binding.productNameText.text = product.displayName

            // Packaging
            binding.productPackagingText.text = product.packaging ?: ""
            binding.productPackagingText.isVisible = !product.packaging.isNullOrBlank()

            // Category
            val categoryName = product.categories?.firstOrNull()?.name
            binding.productCategoryText.text = categoryName ?: ""
            binding.productCategoryText.isVisible = !categoryName.isNullOrBlank()

            // Recommended quantity
            binding.recommendedQuantityText.text = item.recommendedQuantity.toString()

            // Check if product exists in local database
            val localProduct = localProducts.find { it.id == product.id }
            if (localProduct != null) {
                binding.localFrequencyLayout.isVisible = true
                binding.localFrequencyText.text = localProduct.frequency.toString()
                binding.newProductChip.isVisible = false
            } else {
                binding.localFrequencyLayout.isVisible = false
                binding.newProductChip.isVisible = true
            }

            // Price information
            val priceInstructions = product.priceInstructions
            if (priceInstructions != null) {
                // Unit price
                binding.productPriceText.text = binding.root.context.getString(
                    R.string.price_format,
                    priceInstructions.unitPrice
                )

                // Reference price (e.g., price per kg)
                val referencePrice = priceInstructions.referencePrice
                val referenceFormat = priceInstructions.referenceFormat
                if (referencePrice != null && referenceFormat != null) {
                    binding.productReferencePriceText.text = binding.root.context.getString(
                        R.string.reference_price_format,
                        referencePrice,
                        referenceFormat
                    )
                    binding.productReferencePriceText.isVisible = true
                } else {
                    binding.productReferencePriceText.isVisible = false
                }
            } else {
                binding.productPriceText.isVisible = false
                binding.productReferencePriceText.isVisible = false
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<RecommendationItem>() {
        override fun areItemsTheSame(oldItem: RecommendationItem, newItem: RecommendationItem): Boolean {
            return oldItem.product.id == newItem.product.id
        }

        override fun areContentsTheSame(oldItem: RecommendationItem, newItem: RecommendationItem): Boolean {
            return oldItem == newItem
        }
    }
}
