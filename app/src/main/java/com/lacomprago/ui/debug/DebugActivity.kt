package com.lacomprago.ui.debug

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.GsonBuilder
import com.lacomprago.R
import com.lacomprago.data.api.ApiClient
import com.lacomprago.data.api.debug.ApiEndpoint
import com.lacomprago.data.api.debug.HttpMethod
import com.lacomprago.databinding.ActivityDebugBinding
import com.lacomprago.storage.TokenStorage
import com.lacomprago.viewmodel.DebugState
import com.lacomprago.viewmodel.DebugViewModel
import com.lacomprago.viewmodel.TokenStatus

/**
 * Debug Activity for API endpoint testing.
 * 
 * Only available in debug builds. Provides UI for:
 * - Token management (save, clear, validate)
 * - Endpoint selection
 * - Request configuration (path params, query params, body)
 * - Response viewing
 */
class DebugActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDebugBinding
    private lateinit var viewModel: DebugViewModel
    private lateinit var endpointAdapter: EndpointAdapter

    private val gson = GsonBuilder().setPrettyPrinting().create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDebugBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViewModel()
        setupToolbar()
        setupTokenSection()
        setupEndpointList()
        setupRequestSection()
        setupResponseSection()
        observeState()
    }

    private fun initViewModel() {
        val tokenStorage = TokenStorage(applicationContext)
        val apiClient = ApiClient.create(tokenStorage)
        viewModel = DebugViewModel(tokenStorage, apiClient)
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupTokenSection() {
        // Observe token status
        viewModel.tokenStatus.observe(this) { status ->
            when (status) {
                is TokenStatus.None -> {
                    binding.tokenStatusText.text = getString(R.string.debug_token_status_none)
                    binding.tokenStatusText.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
                }
                is TokenStatus.Stored -> {
                    binding.tokenStatusText.text = getString(R.string.debug_token_status_stored)
                    binding.tokenStatusText.setTextColor(ContextCompat.getColor(this, R.color.success_green))
                }
                is TokenStatus.Invalid -> {
                    binding.tokenStatusText.text = getString(R.string.debug_token_status_invalid, status.reason)
                    binding.tokenStatusText.setTextColor(ContextCompat.getColor(this, R.color.error_red))
                }
            }
            binding.tokenStatusText.setTextIsSelectable(true)
        }

        // Observe customer ID
        viewModel.customerId.observe(this) { customerId ->
            if (binding.customerIdEditText.text.toString() != customerId) {
                binding.customerIdEditText.setText(customerId)
            }
        }

        // Setup button listeners
        binding.saveTokenButton.setOnClickListener {
            val token = binding.tokenEditText.text.toString()
            viewModel.saveToken(token)
            binding.tokenEditText.text?.clear()
        }

        binding.clearTokenButton.setOnClickListener {
            viewModel.clearToken()
        }

        binding.saveCustomerIdButton.setOnClickListener {
            val customerId = binding.customerIdEditText.text.toString()
            viewModel.saveCustomerId(customerId)
        }

        binding.validateTokenButton.setOnClickListener {
            viewModel.validateToken()
        }
    }

    private fun setupEndpointList() {
        endpointAdapter = EndpointAdapter { endpoint ->
            viewModel.selectEndpoint(endpoint)
            showEndpointConfig(endpoint)
        }

        binding.endpointsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@DebugActivity)
            adapter = endpointAdapter
        }

        endpointAdapter.submitList(viewModel.endpoints)
    }

    private fun setupRequestSection() {
        binding.sendRequestButton.setOnClickListener {
            viewModel.executeRequest()
        }
    }

    private fun setupResponseSection() {
        binding.copyResponseButton.setOnClickListener {
            val responseText = binding.responseBodyText.text.toString()
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("API Response", responseText)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, R.string.debug_response_copied, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showEndpointConfig(endpoint: ApiEndpoint) {
        binding.requestConfigCard.isVisible = true
        binding.endpointDescriptionText.text = endpoint.description

        // Clear existing parameter inputs
        binding.pathParamsContainer.removeAllViews()
        binding.queryParamsContainer.removeAllViews()

        // Add path parameter inputs
        if (endpoint.pathParams.isNotEmpty()) {
            addSectionLabel(binding.pathParamsContainer, getString(R.string.debug_path_params))
            endpoint.pathParams.forEach { param ->
                addParameterInput(binding.pathParamsContainer, param, viewModel.getPathParam(param)) { value ->
                    viewModel.setPathParam(param, value)
                }
            }
        }

        // Add query parameter inputs
        if (endpoint.queryParams.isNotEmpty()) {
            addSectionLabel(binding.queryParamsContainer, getString(R.string.debug_query_params))
            endpoint.queryParams.forEach { param ->
                addParameterInput(binding.queryParamsContainer, param, viewModel.getQueryParam(param)) { value ->
                    viewModel.setQueryParam(param, value)
                }
            }
        }

        // Show/hide request body section
        binding.requestBodyLayout.isVisible = endpoint.hasBody
        if (endpoint.hasBody) {
            binding.requestBodyEditText.setText(viewModel.getRequestBody() ?: endpoint.sampleBody ?: "")
        }

        // Hide response card when selecting new endpoint
        binding.responseCard.isVisible = false
    }

    private fun addSectionLabel(container: android.widget.LinearLayout, label: String) {
        val textView = android.widget.TextView(this).apply {
            text = label
            setTextColor(ContextCompat.getColor(this@DebugActivity, R.color.text_secondary))
            textSize = 12f
            setPadding(0, 8, 0, 4)
        }
        container.addView(textView)
    }

    private fun addParameterInput(
        container: android.widget.LinearLayout,
        paramName: String,
        initialValue: String,
        onValueChanged: (String) -> Unit
    ) {
        val inflater = LayoutInflater.from(this)
        val inputLayout = TextInputLayout(this).apply {
            hint = paramName
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 8)
            }
        }

        val editText = TextInputEditText(inputLayout.context).apply {
            setText(initialValue)
            setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    onValueChanged(text.toString())
                }
            }
        }

        inputLayout.addView(editText)
        container.addView(inputLayout)
    }

    private fun observeState() {
        viewModel.state.observe(this) { state ->
            when (state) {
                is DebugState.Idle -> {
                    binding.loadingIndicator.isVisible = false
                }
                is DebugState.Preparing -> {
                    binding.loadingIndicator.isVisible = false
                }
                is DebugState.Loading -> {
                    binding.loadingIndicator.isVisible = true
                    binding.responseCard.isVisible = false
                }
                is DebugState.Success -> {
                    binding.loadingIndicator.isVisible = false
                    showResponse(
                        statusCode = state.response.statusCode,
                        statusMessage = state.response.statusMessage,
                        body = state.response.body,
                        durationMs = state.response.durationMs,
                        isError = false,
                        requestInfo = state.request?.let { "${it.method} ${it.url}" } ?: ""
                    )
                }
                is DebugState.Error -> {
                    binding.loadingIndicator.isVisible = false
                    if (state.response != null) {
                        showResponse(
                            statusCode = state.response.statusCode,
                            statusMessage = state.response.statusMessage,
                            body = state.response.body ?: state.message,
                            durationMs = state.response.durationMs,
                            isError = true,
                            requestInfo = state.request?.let { "${it.method} ${it.url}" } ?: ""
                        )
                    } else {
                        showResponse(
                            statusCode = -1,
                            statusMessage = "Error",
                            body = state.message,
                            durationMs = 0,
                            isError = true,
                            requestInfo = state.request?.let { "${it.method} ${it.url}" } ?: ""
                        )
                    }
                }
            }
        }

        // Also observe selected endpoint to update request body changes
        viewModel.selectedEndpoint.observe(this) { endpoint ->
            if (endpoint?.hasBody == true) {
                binding.requestBodyEditText.addTextChangedListener(object : android.text.TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                    override fun afterTextChanged(s: android.text.Editable?) {
                        viewModel.setRequestBody(s?.toString())
                    }
                })
            }
        }
    }

    private fun showResponse(
        statusCode: Int,
        statusMessage: String,
        body: String?,
        durationMs: Long,
        isError: Boolean,
        requestInfo: String
    ) {
        binding.responseCard.isVisible = true

        binding.responseStatusText.text = getString(R.string.debug_response_status, statusCode, statusMessage)
        binding.responseStatusText.setTextColor(
            ContextCompat.getColor(this, if (isError) R.color.error_red else R.color.success_green)
        )

        binding.responseRequestText.isVisible = requestInfo.isNotBlank()
        binding.responseRequestText.text = requestInfo

        binding.responseDurationText.text = getString(R.string.debug_response_duration, durationMs)

        // Try to pretty-print JSON
        val formattedBody = try {
            if (body != null) {
                val jsonElement = com.google.gson.JsonParser.parseString(body)
                gson.toJson(jsonElement)
            } else {
                "(empty response)"
            }
        } catch (e: Exception) {
            body ?: "(empty response)"
        }

        binding.responseBodyText.text = formattedBody
    }
}
