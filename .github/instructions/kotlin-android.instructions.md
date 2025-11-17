---
applyTo: "**/*.kt"
description: "Kotlin and Android-specific coding guidelines for LaCompraGo project"
---

# Kotlin & Android Development Instructions

## 🎯 Context

This file provides detailed Kotlin and Android-specific guidance for the LaCompraGo project. These instructions apply to all Kotlin files in the project.

## 🔧 Kotlin Language Features

### Idiomatic Kotlin

#### Data Classes
```kotlin
// ✅ Good: Use data classes for models
data class Product(
    val id: String,
    val name: String,
    val frequency: Int = 0,
    val lastPurchase: Long? = null,
    val category: String
)

// ❌ Avoid: Regular classes for simple data containers
class Product(val id: String, val name: String) {
    // ...
}
```

#### Sealed Classes for State Management
```kotlin
// ✅ Good: Use sealed classes for UI states
sealed class ProductState {
    object Loading : ProductState()
    data class Success(val products: List<Product>) : ProductState()
    data class Error(val message: String) : ProductState()
}

// ✅ Good: Use when expressions with sealed classes
fun handleState(state: ProductState) {
    when (state) {
        is ProductState.Loading -> showLoading()
        is ProductState.Success -> showProducts(state.products)
        is ProductState.Error -> showError(state.message)
    }
}
```

#### Null Safety
```kotlin
// ✅ Good: Use safe calls and Elvis operator
val productName = product?.name ?: "Unknown"

// ✅ Good: Use let for null checks
product?.let { 
    processProduct(it)
}

// ❌ Avoid: Force unwrapping with !!
val name = product!!.name // Dangerous!

// ✅ Good: Early returns with null checks
fun processOrder(order: Order?) {
    val validOrder = order ?: run {
        Log.e(TAG, "Order is null")
        return
    }
    // Process validOrder safely
}
```

#### Extension Functions
```kotlin
// ✅ Good: Use extensions for utility functions
fun String.isValidToken(): Boolean {
    return this.length >= 32 && this.matches(Regex("[A-Za-z0-9]+"))
}

fun Long.toFormattedDate(): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return dateFormat.format(Date(this))
}

// Usage
val isValid = token.isValidToken()
val dateStr = timestamp.toFormattedDate()
```

#### Scoped Functions
```kotlin
// ✅ Good: Use apply for object configuration
val request = Request.Builder().apply {
    url(apiUrl)
    addHeader("Authorization", "Bearer $token")
    post(body)
}.build()

// ✅ Good: Use let for transformations and null checks
val result = product?.let { product ->
    product.name.uppercase()
}

// ✅ Good: Use run for grouping operations
val products = run {
    val json = readJsonFile()
    gson.fromJson(json, ProductList::class.java)
}

// ✅ Good: Use also for side effects
fun createProduct(): Product {
    return Product(id = "123", name = "Milk").also {
        Log.d(TAG, "Created product: ${it.name}")
    }
}

// ✅ Good: Use with for multiple calls on same object
with(binding) {
    progressBar.visibility = View.VISIBLE
    errorText.visibility = View.GONE
    recyclerView.adapter = productAdapter
}
```

### Collections and Sequences

```kotlin
// ✅ Good: Use collection operations
val activeProducts = products
    .filter { it.frequency > 0 }
    .sortedByDescending { it.frequency }
    .take(10)

// ✅ Good: Use sequences for large collections
val topProducts = products.asSequence()
    .filter { it.frequency > 5 }
    .sortedByDescending { it.lastPurchase }
    .take(20)
    .toList()

// ✅ Good: Use associateBy for list-to-map conversion
val productMap = products.associateBy { it.id }

// ✅ Good: Use groupBy for categorization
val productsByCategory = products.groupBy { it.category }
```

### String Templates
```kotlin
// ✅ Good: Use string templates
val message = "Processing order ${order.id} with ${order.items.size} items"

// ✅ Good: Use multiline strings for JSON or SQL
val jsonTemplate = """
    {
        "id": "${product.id}",
        "name": "${product.name}",
        "frequency": ${product.frequency}
    }
""".trimIndent()
```

## 🏗️ Android Patterns

### ViewModel Implementation
```kotlin
// ✅ Good: ViewModel with sealed class states and LiveData
class ProductViewModel : ViewModel() {
    private val _productState = MutableLiveData<ProductState>()
    val productState: LiveData<ProductState> = _productState
    
    private val repository = ProductRepository()
    
    fun loadProducts() {
        viewModelScope.launch {
            _productState.value = ProductState.Loading
            try {
                val products = repository.getProducts()
                _productState.value = ProductState.Success(products)
            } catch (e: Exception) {
                _productState.value = ProductState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    fun refreshProducts(token: String) {
        viewModelScope.launch {
            _productState.value = ProductState.Loading
            try {
                repository.processOrders(token)
                val products = repository.getProducts()
                _productState.value = ProductState.Success(products)
            } catch (e: Exception) {
                _productState.value = ProductState.Error(e.message ?: "Failed to refresh")
            }
        }
    }
}
```

### Repository Pattern
```kotlin
// ✅ Good: Repository with coroutines and proper error handling
class ProductRepository {
    private val gson = Gson()
    private val productsFile = "products.json"
    
    suspend fun getProducts(): List<Product> = withContext(Dispatchers.IO) {
        try {
            val json = readJsonFile(productsFile)
            val productList = gson.fromJson(json, ProductList::class.java)
            productList.products
        } catch (e: Exception) {
            Log.e(TAG, "Error reading products", e)
            emptyList()
        }
    }
    
    suspend fun saveProducts(products: List<Product>) = withContext(Dispatchers.IO) {
        try {
            val productList = ProductList(products)
            val json = gson.toJson(productList)
            writeJsonFile(productsFile, json)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving products", e)
            throw e
        }
    }
    
    private fun readJsonFile(filename: String): String {
        // File I/O implementation
        return ""
    }
    
    private fun writeJsonFile(filename: String, content: String) {
        // File I/O implementation
    }
    
    companion object {
        private const val TAG = "ProductRepository"
    }
}
```

### Activity with View Binding
```kotlin
// ✅ Good: Activity with View Binding and LiveData observation
class ProductListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductListBinding
    private val viewModel: ProductViewModel by viewModels()
    private lateinit var adapter: ProductAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupRecyclerView()
        setupObservers()
        setupListeners()
        
        viewModel.loadProducts()
    }
    
    private fun setupRecyclerView() {
        adapter = ProductAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ProductListActivity)
            adapter = this@ProductListActivity.adapter
        }
    }
    
    private fun setupObservers() {
        viewModel.productState.observe(this) { state ->
            when (state) {
                is ProductState.Loading -> showLoading()
                is ProductState.Success -> showProducts(state.products)
                is ProductState.Error -> showError(state.message)
            }
        }
    }
    
    private fun setupListeners() {
        binding.refreshButton.setOnClickListener {
            val token = getToken() // Get from secure storage
            viewModel.refreshProducts(token)
        }
    }
    
    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
        binding.errorText.visibility = View.GONE
    }
    
    private fun showProducts(products: List<Product>) {
        binding.progressBar.visibility = View.GONE
        binding.recyclerView.visibility = View.VISIBLE
        binding.errorText.visibility = View.GONE
        adapter.submitList(products)
    }
    
    private fun showError(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.recyclerView.visibility = View.GONE
        binding.errorText.visibility = View.VISIBLE
        binding.errorText.text = message
    }
}
```

### Coroutines Usage
```kotlin
// ✅ Good: Use appropriate dispatchers
class OrderRepository {
    // IO Dispatcher for network and file operations
    suspend fun fetchOrder(orderId: String): Order = withContext(Dispatchers.IO) {
        val response = apiClient.fetchOrder(orderId)
        response.body() ?: throw IOException("Empty response")
    }
    
    // Main dispatcher for UI updates (implicit when using LiveData)
    fun updateUI() {
        viewModelScope.launch {
            // This runs on Main dispatcher by default
            _state.value = State.Success
        }
    }
}

// ✅ Good: Proper cancellation handling
class OrderProcessingViewModel : ViewModel() {
    private var processingJob: Job? = null
    
    fun startProcessing() {
        processingJob = viewModelScope.launch {
            try {
                processOrders()
            } catch (e: CancellationException) {
                // Handle cancellation gracefully
                Log.d(TAG, "Processing cancelled")
                throw e // Re-throw to propagate cancellation
            }
        }
    }
    
    fun cancelProcessing() {
        processingJob?.cancel()
    }
}

// ✅ Good: Error handling in coroutines
suspend fun processOrder(orderId: String): Result<Order> = withContext(Dispatchers.IO) {
    try {
        val order = fetchOrder(orderId)
        Result.success(order)
    } catch (e: Exception) {
        Log.e(TAG, "Error processing order $orderId", e)
        Result.failure(e)
    }
}
```

### OkHttp Network Calls
```kotlin
// ✅ Good: OkHttp client with proper configuration
class ApiClient(private val token: String) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Content-Type", "application/json")
                .build()
            chain.proceed(request)
        }
        .build()
    
    suspend fun fetchOrders(): List<String> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("${ApiConfig.BASE_URL}/orders")
            .get()
            .build()
        
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Unexpected response ${response.code}")
            }
            
            val json = response.body?.string() 
                ?: throw IOException("Empty response body")
            
            val orderList = gson.fromJson(json, OrderListResponse::class.java)
            orderList.orderIds
        }
    }
}

// ✅ Good: Use 'use' to ensure resources are closed
fun readFromStream(inputStream: InputStream): String {
    return inputStream.bufferedReader().use { it.readText() }
}
```

### Secure Storage
```kotlin
// ✅ Good: EncryptedSharedPreferences for sensitive data
class TokenStorage(private val context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    fun saveToken(token: String) {
        sharedPreferences.edit {
            putString(KEY_TOKEN, token)
        }
    }
    
    fun getToken(): String? {
        return sharedPreferences.getString(KEY_TOKEN, null)
    }
    
    fun clearToken() {
        sharedPreferences.edit {
            remove(KEY_TOKEN)
        }
    }
    
    companion object {
        private const val KEY_TOKEN = "api_token"
    }
}
```

## 🧪 Testing Patterns

### Unit Tests
```kotlin
// ✅ Good: Unit test with clear structure
class ProductRepositoryTest {
    private lateinit var repository: ProductRepository
    private lateinit var mockContext: Context
    
    @Before
    fun setup() {
        mockContext = mock(Context::class.java)
        repository = ProductRepository(mockContext)
    }
    
    @Test
    fun `getProducts returns empty list when file not found`() = runBlocking {
        // Arrange
        `when`(mockContext.openFileInput(any())).thenThrow(FileNotFoundException())
        
        // Act
        val products = repository.getProducts()
        
        // Assert
        assertTrue(products.isEmpty())
    }
    
    @Test
    fun `saveProducts writes correct JSON format`() = runBlocking {
        // Arrange
        val products = listOf(
            Product("1", "Milk", 5, System.currentTimeMillis(), "Dairy")
        )
        
        // Act
        repository.saveProducts(products)
        
        // Assert
        verify(mockContext).openFileOutput(eq("products.json"), eq(Context.MODE_PRIVATE))
    }
}
```

## 🚫 Anti-Patterns to Avoid

### Memory Leaks
```kotlin
// ❌ Bad: Holding Activity reference in background thread
class BadViewModel(private val activity: Activity) : ViewModel() {
    // This will leak the activity!
}

// ✅ Good: Use application context or no context at all
class GoodViewModel(application: Application) : AndroidViewModel(application) {
    private val appContext = application.applicationContext
}
```

### Blocking Main Thread
```kotlin
// ❌ Bad: Network call on main thread
fun fetchData() {
    val response = apiClient.fetchOrders() // Blocks UI!
    updateUI(response)
}

// ✅ Good: Use coroutines with IO dispatcher
fun fetchData() {
    viewModelScope.launch {
        val response = withContext(Dispatchers.IO) {
            apiClient.fetchOrders()
        }
        updateUI(response)
    }
}
```

### Resource Leaks
```kotlin
// ❌ Bad: Not closing streams
fun readFile(file: File): String {
    val input = FileInputStream(file)
    return input.bufferedReader().readText()
    // Stream not closed!
}

// ✅ Good: Use 'use' to auto-close
fun readFile(file: File): String {
    return file.inputStream().bufferedReader().use { it.readText() }
}
```

## 📝 Documentation

### KDoc Format
```kotlin
/**
 * Processes a list of orders and updates product frequencies.
 *
 * This function fetches orders one by one, extracts products from each order,
 * and updates the local product database with purchase frequency and timestamp.
 *
 * @param token The authentication token for API access
 * @param onProgress Callback to report progress (current order number, total orders)
 * @return Result containing success or error information
 * @throws IOException if network request fails
 * @throws JsonParseException if response cannot be parsed
 */
suspend fun processOrders(
    token: String,
    onProgress: (current: Int, total: Int) -> Unit
): Result<Unit>
```

## 🎯 Summary

- Write idiomatic Kotlin using language features effectively
- Use sealed classes for state management
- Handle nullability explicitly and safely
- Leverage coroutines for async operations with proper dispatchers
- Use View Binding for type-safe view access
- Follow Android lifecycle patterns
- Implement proper error handling
- Write testable, maintainable code
- Document public APIs with KDoc
- Avoid common Android pitfalls (memory leaks, blocking main thread)

Remember: **Simple, clean, and functional code is better than clever code.**
