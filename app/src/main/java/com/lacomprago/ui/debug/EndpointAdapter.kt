package com.lacomprago.ui.debug

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lacomprago.R
import com.lacomprago.data.api.debug.ApiEndpoint
import com.lacomprago.data.api.debug.HttpMethod
import com.lacomprago.databinding.ItemEndpointBinding

/**
 * RecyclerView adapter for displaying API endpoints in debug mode.
 *
 * @param onEndpointClick Callback when an endpoint is selected
 */
class EndpointAdapter(
    private val onEndpointClick: (ApiEndpoint) -> Unit
) : ListAdapter<ApiEndpoint, EndpointAdapter.EndpointViewHolder>(EndpointDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EndpointViewHolder {
        val binding = ItemEndpointBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EndpointViewHolder(binding, onEndpointClick)
    }

    override fun onBindViewHolder(holder: EndpointViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class EndpointViewHolder(
        private val binding: ItemEndpointBinding,
        private val onEndpointClick: (ApiEndpoint) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(endpoint: ApiEndpoint) {
            binding.endpointNameText.text = endpoint.name
            binding.endpointPathText.text = endpoint.path
            
            // Set method badge color based on HTTP method
            val badgeColor = when (endpoint.method) {
                HttpMethod.GET -> R.color.method_get
                HttpMethod.POST -> R.color.method_post
                HttpMethod.PUT -> R.color.method_put
                HttpMethod.DELETE -> R.color.method_delete
            }
            
            binding.methodBadge.text = endpoint.method.name
            binding.methodBadge.background.setTint(
                ContextCompat.getColor(binding.root.context, badgeColor)
            )

            binding.root.setOnClickListener {
                onEndpointClick(endpoint)
            }
        }
    }

    private class EndpointDiffCallback : DiffUtil.ItemCallback<ApiEndpoint>() {
        override fun areItemsTheSame(oldItem: ApiEndpoint, newItem: ApiEndpoint): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ApiEndpoint, newItem: ApiEndpoint): Boolean {
            return oldItem == newItem
        }
    }
}
