package com.lacomprago.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lacomprago.databinding.ItemProductBinding
import com.lacomprago.model.Product
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * RecyclerView adapter for displaying products in a list.
 * Uses ListAdapter with DiffUtil for efficient updates.
 */
class ProductAdapter : ListAdapter<Product, ProductAdapter.ProductViewHolder>(ProductDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    /**
     * ViewHolder for product items.
     */
    class ProductViewHolder(
        private val binding: ItemProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(product: Product) {
            binding.productName.text = product.name
            binding.productFrequency.text = "Frequency: ${product.frequency}"
            binding.productLastPurchase.text = formatLastPurchase(product.lastPurchase)
            
            // Show category if available
            if (product.category != null) {
                binding.productCategory.text = product.category
                binding.productCategory.visibility = android.view.View.VISIBLE
            } else {
                binding.productCategory.visibility = android.view.View.GONE
            }
        }
        
        /**
         * Format the last purchase timestamp into a human-readable string.
         * Shows "X days ago" for recent purchases, or a date for older ones.
         */
        private fun formatLastPurchase(timestamp: Long): String {
            val now = System.currentTimeMillis()
            val diffMillis = now - timestamp
            val diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis)
            
            return when {
                diffDays == 0L -> "Today"
                diffDays == 1L -> "1 day ago"
                diffDays < 7 -> "$diffDays days ago"
                diffDays < 14 -> "1 week ago"
                diffDays < 21 -> "2 weeks ago"
                diffDays < 28 -> "3 weeks ago"
                else -> {
                    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                    dateFormat.format(Date(timestamp))
                }
            }
        }
    }
    
    /**
     * DiffUtil callback for efficient list updates.
     */
    private class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }
}
