package com.lacomprago.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.lacomprago.R
import com.lacomprago.data.api.ApiClient
import com.lacomprago.model.OrderProcessingState
import com.lacomprago.storage.JsonStorage
import com.lacomprago.storage.TokenStorage
import com.lacomprago.viewmodel.OrderProcessingViewModel

/**
 * Dialog fragment for displaying order processing progress.
 * Shows progress bar, status text, and allows cancellation.
 * 
 * Note: To avoid API rate limiting, only ONE order is processed per sync.
 */
class OrderProcessingDialogFragment : DialogFragment() {
    
    private lateinit var viewModel: OrderProcessingViewModel
    
    private lateinit var progressBar: LinearProgressIndicator
    private lateinit var statusText: TextView
    private lateinit var orderIdText: TextView
    private lateinit var resultMessage: TextView
    private lateinit var cancelButton: MaterialButton
    private lateinit var okButton: MaterialButton
    
    private var onProcessingComplete: (() -> Unit)? = null
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()
        
        // Initialize ViewModel dependencies
        val tokenStorage = TokenStorage(context)
        val apiClient = ApiClient.create(tokenStorage)
        val jsonStorage = JsonStorage(context)
        viewModel = OrderProcessingViewModel(apiClient, jsonStorage, tokenStorage)

        // Inflate the custom layout
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_order_processing, null)
        
        // Initialize views
        progressBar = view.findViewById(R.id.progressBar)
        statusText = view.findViewById(R.id.statusText)
        orderIdText = view.findViewById(R.id.orderIdText)
        resultMessage = view.findViewById(R.id.resultMessage)
        cancelButton = view.findViewById(R.id.cancelButton)
        okButton = view.findViewById(R.id.okButton)
        
        // Set up cancel button
        cancelButton.setOnClickListener {
            viewModel.cancelProcessing()
        }
        
        // Set up OK button (shown on completion)
        okButton.setOnClickListener {
            viewModel.resetState()
            dismiss()
        }
        
        // Observe processing state
        viewModel.processingState.observe(this) { state ->
            updateUI(state)
        }
        
        // Start processing
        viewModel.startProcessing()
        
        // Create dialog
        return MaterialAlertDialogBuilder(context)
            .setView(view)
            .setCancelable(false)
            .create()
    }
    
    /**
     * Set callback for when processing is complete.
     * Used to refresh the product list after processing.
     */
    fun setOnProcessingCompleteListener(listener: () -> Unit) {
        onProcessingComplete = listener
    }
    
    private fun updateUI(state: OrderProcessingState) {
        when (state) {
            is OrderProcessingState.Idle -> {
                // Initial state - nothing to show
            }
            
            is OrderProcessingState.FetchingOrders -> {
                showProcessingState()
                progressBar.isIndeterminate = true
                statusText.text = getString(R.string.fetching_orders)
                orderIdText.visibility = View.GONE
            }
            
            is OrderProcessingState.Processing -> {
                showProcessingState()
                progressBar.isIndeterminate = true
                statusText.text = getString(R.string.processing_status)
                orderIdText.text = getString(R.string.order_id_format, state.currentOrderId)
                orderIdText.visibility = View.VISIBLE
            }
            
            is OrderProcessingState.Completed -> {
                showResultState()
                if (state.updatedProductCount > 0) {
                    if (state.remainingOrders > 0) {
                        resultMessage.text = getString(
                            R.string.processing_complete_remaining,
                            state.updatedProductCount,
                            state.remainingOrders
                        )
                    } else {
                        resultMessage.text = getString(
                            R.string.processing_complete,
                            state.updatedProductCount
                        )
                    }
                    resultMessage.setTextColor(resources.getColor(R.color.success_green, null))
                } else {
                    resultMessage.text = getString(R.string.processing_complete_none)
                    resultMessage.setTextColor(resources.getColor(R.color.text_secondary, null))
                }
                onProcessingComplete?.invoke()
            }
            
            is OrderProcessingState.Cancelled -> {
                showResultState()
                resultMessage.text = getString(R.string.processing_cancelled)
                resultMessage.setTextColor(resources.getColor(R.color.text_secondary, null))
            }
            
            is OrderProcessingState.Error -> {
                showResultState()
                resultMessage.text = getString(R.string.processing_error, state.message)
                resultMessage.setTextColor(resources.getColor(R.color.error_red, null))
            }
        }
    }
    
    private fun showProcessingState() {
        progressBar.visibility = View.VISIBLE
        statusText.visibility = View.VISIBLE
        resultMessage.visibility = View.GONE
        cancelButton.visibility = View.VISIBLE
        okButton.visibility = View.GONE
    }
    
    private fun showResultState() {
        progressBar.visibility = View.GONE
        statusText.visibility = View.GONE
        orderIdText.visibility = View.GONE
        resultMessage.visibility = View.VISIBLE
        cancelButton.visibility = View.GONE
        okButton.visibility = View.VISIBLE
    }
    
    companion object {
        const val TAG = "OrderProcessingDialog"
        
        fun newInstance(): OrderProcessingDialogFragment {
            return OrderProcessingDialogFragment()
        }
    }
}
