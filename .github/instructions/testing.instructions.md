---
applyTo: ["**/test/**/*.kt", "**/androidTest/**/*.kt"]
description: "Testing guidelines and patterns for LaCompraGo project"
---

# Testing Instructions for LaCompraGo

## 🧪 Testing Philosophy

Testing in LaCompraGo follows these principles:
- **Simple and Practical**: Focus on testing critical functionality
- **Maintainable**: Tests should be easy to understand and update
- **Fast**: Unit tests should run quickly
- **Comprehensive**: Cover main use cases and edge cases

## 📁 Test Structure

```
app/src/
  test/                    # Unit tests (JVM)
    java/com/lacomprago/
      viewmodel/           # ViewModel tests
      repository/          # Repository tests
      model/               # Model and data class tests
      util/                # Utility function tests
  
  androidTest/             # Instrumented tests (Android device/emulator)
    java/com/lacomprago/
      ui/                  # UI tests
      integration/         # Integration tests
      storage/             # Storage and file I/O tests
```

## 🔬 Unit Testing (JVM Tests)

### Test Framework
- **JUnit 4** for test structure
- **Kotlin Coroutines Test** for testing suspending functions
- **Mockito** or simple mocking for dependencies

### ViewModel Testing

```kotlin
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class ProductViewModelTest {
    
    // Rule to make LiveData work synchronously in tests
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    // Test dispatcher for coroutines
    private val testDispatcher = StandardTestDispatcher()
    
    private lateinit var viewModel: ProductViewModel
    private lateinit var mockRepository: ProductRepository
    
    @Before
    fun setup() {
        // Set test dispatcher as Main dispatcher
        Dispatchers.setMain(testDispatcher)
        
        // Create mock repository (can use Mockito or manual mock)
        mockRepository = TestProductRepository()
        viewModel = ProductViewModel(mockRepository)
    }
    
    @After
    fun tearDown() {
        // Reset Main dispatcher
        Dispatchers.resetMain()
    }
    
    @Test
    fun `loadProducts sets Loading state initially`() = runTest {
        // Arrange
        val states = mutableListOf<ProductState>()
        viewModel.productState.observeForever { states.add(it) }
        
        // Act
        viewModel.loadProducts()
        advanceUntilIdle() // Process all coroutines
        
        // Assert
        assertTrue(states[0] is ProductState.Loading)
    }
    
    @Test
    fun `loadProducts sets Success state with products`() = runTest {
        // Arrange
        val expectedProducts = listOf(
            Product("1", "Milk", 5, 1234567890L, "Dairy"),
            Product("2", "Bread", 3, 1234567800L, "Bakery")
        )
        mockRepository.setProducts(expectedProducts)
        
        val states = mutableListOf<ProductState>()
        viewModel.productState.observeForever { states.add(it) }
        
        // Act
        viewModel.loadProducts()
        advanceUntilIdle()
        
        // Assert
        val successState = states.last() as ProductState.Success
        assertEquals(expectedProducts, successState.products)
    }
    
    @Test
    fun `loadProducts sets Error state on exception`() = runTest {
        // Arrange
        mockRepository.setShouldThrowError(true)
        
        val states = mutableListOf<ProductState>()
        viewModel.productState.observeForever { states.add(it) }
        
        // Act
        viewModel.loadProducts()
        advanceUntilIdle()
        
        // Assert
        assertTrue(states.last() is ProductState.Error)
    }
    
    @Test
    fun `refreshProducts processes orders and updates products`() = runTest {
        // Arrange
        val token = "test_token_123"
        val initialProducts = listOf(Product("1", "Milk", 5, 1234567890L, "Dairy"))
        val updatedProducts = listOf(Product("1", "Milk", 6, 1234567900L, "Dairy"))
        
        mockRepository.setProducts(initialProducts)
        mockRepository.setUpdatedProducts(updatedProducts)
        
        val states = mutableListOf<ProductState>()
        viewModel.productState.observeForever { states.add(it) }
        
        // Act
        viewModel.refreshProducts(token)
        advanceUntilIdle()
        
        // Assert
        val successState = states.last() as ProductState.Success
        assertEquals(6, successState.products[0].frequency)
    }
}

// Simple test implementation of repository
class TestProductRepository : ProductRepository {
    private var products: List<Product> = emptyList()
    private var updatedProducts: List<Product> = emptyList()
    private var shouldThrowError = false
    
    fun setProducts(products: List<Product>) {
        this.products = products
    }
    
    fun setUpdatedProducts(products: List<Product>) {
        this.updatedProducts = products
    }
    
    fun setShouldThrowError(shouldThrow: Boolean) {
        this.shouldThrowError = shouldThrow
    }
    
    override suspend fun getProducts(): List<Product> {
        if (shouldThrowError) throw IOException("Test error")
        return products
    }
    
    override suspend fun processOrders(token: String) {
        if (shouldThrowError) throw IOException("Test error")
        products = updatedProducts
    }
}
```

### Repository Testing

```kotlin
class ProductRepositoryTest {
    
    private lateinit var repository: ProductRepository
    private lateinit var testContext: Context
    private lateinit var tempDir: File
    
    @Before
    fun setup() {
        // Create temporary directory for test files
        tempDir = Files.createTempDirectory("test").toFile()
        testContext = mock(Context::class.java)
        `when`(testContext.filesDir).thenReturn(tempDir)
        
        repository = ProductRepository(testContext)
    }
    
    @After
    fun tearDown() {
        // Clean up test files
        tempDir.deleteRecursively()
    }
    
    @Test
    fun `getProducts returns empty list when file does not exist`() = runBlocking {
        // Act
        val products = repository.getProducts()
        
        // Assert
        assertTrue(products.isEmpty())
    }
    
    @Test
    fun `saveProducts and getProducts work correctly`() = runBlocking {
        // Arrange
        val testProducts = listOf(
            Product("1", "Milk", 5, 1234567890L, "Dairy"),
            Product("2", "Bread", 3, 1234567800L, "Bakery")
        )
        
        // Act
        repository.saveProducts(testProducts)
        val retrievedProducts = repository.getProducts()
        
        // Assert
        assertEquals(testProducts.size, retrievedProducts.size)
        assertEquals(testProducts[0].id, retrievedProducts[0].id)
        assertEquals(testProducts[0].frequency, retrievedProducts[0].frequency)
    }
    
    @Test
    fun `updateProductFrequency increments frequency correctly`() = runBlocking {
        // Arrange
        val product = Product("1", "Milk", 5, 1234567890L, "Dairy")
        repository.saveProducts(listOf(product))
        
        // Act
        repository.updateProductFrequency("1", 1234567900L)
        val products = repository.getProducts()
        
        // Assert
        assertEquals(6, products[0].frequency)
        assertEquals(1234567900L, products[0].lastPurchase)
    }
    
    @Test(expected = IOException::class)
    fun `saveProducts throws exception on write failure`() = runBlocking {
        // Arrange
        val readOnlyDir = File(tempDir, "readonly").apply {
            mkdirs()
            setWritable(false)
        }
        val context = mock(Context::class.java)
        `when`(context.filesDir).thenReturn(readOnlyDir)
        val repo = ProductRepository(context)
        
        // Act
        repo.saveProducts(listOf(Product("1", "Test", 1, 0L, "Test")))
        
        // Assert: Exception thrown
    }
}
```

### Model and Data Class Testing

```kotlin
class ProductTest {
    
    @Test
    fun `Product data class equality works correctly`() {
        // Arrange
        val product1 = Product("1", "Milk", 5, 1234567890L, "Dairy")
        val product2 = Product("1", "Milk", 5, 1234567890L, "Dairy")
        val product3 = Product("2", "Bread", 3, 1234567800L, "Bakery")
        
        // Assert
        assertEquals(product1, product2)
        assertNotEquals(product1, product3)
    }
    
    @Test
    fun `Product copy works correctly`() {
        // Arrange
        val original = Product("1", "Milk", 5, 1234567890L, "Dairy")
        
        // Act
        val modified = original.copy(frequency = 6)
        
        // Assert
        assertEquals(5, original.frequency)
        assertEquals(6, modified.frequency)
        assertEquals(original.id, modified.id)
    }
    
    @Test
    fun `Product serialization to JSON works correctly`() {
        // Arrange
        val product = Product("1", "Milk", 5, 1234567890L, "Dairy")
        val gson = Gson()
        
        // Act
        val json = gson.toJson(product)
        val deserialized = gson.fromJson(json, Product::class.java)
        
        // Assert
        assertEquals(product, deserialized)
    }
}
```

### Utility Function Testing

```kotlin
class ExtensionFunctionsTest {
    
    @Test
    fun `String isValidToken returns true for valid tokens`() {
        // Arrange
        val validToken = "abcdefghijklmnopqrstuvwxyz123456"
        
        // Act
        val result = validToken.isValidToken()
        
        // Assert
        assertTrue(result)
    }
    
    @Test
    fun `String isValidToken returns false for short tokens`() {
        // Arrange
        val shortToken = "abc123"
        
        // Act
        val result = shortToken.isValidToken()
        
        // Assert
        assertFalse(result)
    }
    
    @Test
    fun `String isValidToken returns false for tokens with invalid characters`() {
        // Arrange
        val invalidToken = "abcd1234!@#$%^&*()"
        
        // Act
        val result = invalidToken.isValidToken()
        
        // Assert
        assertFalse(result)
    }
    
    @Test
    fun `Long toFormattedDate formats correctly`() {
        // Arrange
        val timestamp = 1698765432000L // 2023-10-31
        
        // Act
        val formatted = timestamp.toFormattedDate()
        
        // Assert
        assertEquals("2023-10-31", formatted)
    }
}
```

## 🤖 Instrumented Testing (Android Tests)

### UI Testing with Espresso

```kotlin
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TokenInputActivityTest {
    
    @get:Rule
    val activityRule = ActivityScenarioRule(TokenInputActivity::class.java)
    
    @Test
    fun testTokenInputAndSubmit() {
        // Arrange
        val testToken = "abcdefghijklmnopqrstuvwxyz123456"
        
        // Act
        onView(withId(R.id.tokenEditText))
            .perform(typeText(testToken), closeSoftKeyboard())
        
        onView(withId(R.id.submitButton))
            .perform(click())
        
        // Assert - verify navigation or success state
        onView(withId(R.id.successMessage))
            .check(matches(isDisplayed()))
    }
    
    @Test
    fun testInvalidTokenShowsError() {
        // Arrange
        val invalidToken = "short"
        
        // Act
        onView(withId(R.id.tokenEditText))
            .perform(typeText(invalidToken), closeSoftKeyboard())
        
        onView(withId(R.id.submitButton))
            .perform(click())
        
        // Assert
        onView(withId(R.id.errorText))
            .check(matches(isDisplayed()))
            .check(matches(withText(containsString("invalid"))))
    }
}
```

### Integration Testing

```kotlin
@RunWith(AndroidJUnit4::class)
class OrderProcessingIntegrationTest {
    
    private lateinit var repository: OrderRepository
    private lateinit var context: Context
    
    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        repository = OrderRepository(context)
    }
    
    @Test
    fun testCompleteOrderProcessingFlow() = runBlocking {
        // Arrange
        val token = "test_token_for_integration"
        
        // Act
        repository.processOrders(token) { current, total ->
            Log.d("Test", "Processing $current of $total")
        }
        
        val products = repository.getProducts()
        
        // Assert
        assertTrue(products.isNotEmpty())
        assertTrue(products.all { it.frequency > 0 })
    }
}
```

### Storage Testing

```kotlin
@RunWith(AndroidJUnit4::class)
class TokenStorageTest {
    
    private lateinit var tokenStorage: TokenStorage
    private lateinit var context: Context
    
    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        tokenStorage = TokenStorage(context)
    }
    
    @After
    fun tearDown() {
        tokenStorage.clearToken()
    }
    
    @Test
    fun testSaveAndRetrieveToken() {
        // Arrange
        val testToken = "test_token_12345678901234567890"
        
        // Act
        tokenStorage.saveToken(testToken)
        val retrievedToken = tokenStorage.getToken()
        
        // Assert
        assertEquals(testToken, retrievedToken)
    }
    
    @Test
    fun testClearToken() {
        // Arrange
        val testToken = "test_token_12345678901234567890"
        tokenStorage.saveToken(testToken)
        
        // Act
        tokenStorage.clearToken()
        val retrievedToken = tokenStorage.getToken()
        
        // Assert
        assertNull(retrievedToken)
    }
    
    @Test
    fun testTokenPersistsAcrossInstances() {
        // Arrange
        val testToken = "test_token_12345678901234567890"
        tokenStorage.saveToken(testToken)
        
        // Act - create new instance
        val newStorage = TokenStorage(context)
        val retrievedToken = newStorage.getToken()
        
        // Assert
        assertEquals(testToken, retrievedToken)
    }
}
```

## 🎯 Testing Best Practices

### Test Naming
```kotlin
// ✅ Good: Descriptive test names
@Test
fun `getProducts returns empty list when file does not exist`()

@Test
fun `loadProducts sets Error state when repository throws exception`()

// ❌ Avoid: Vague test names
@Test
fun testGetProducts()

@Test
fun testError()
```

### AAA Pattern (Arrange-Act-Assert)
```kotlin
@Test
fun `updateProductFrequency increments frequency correctly`() = runBlocking {
    // Arrange - Set up test data and state
    val product = Product("1", "Milk", 5, 1234567890L, "Dairy")
    repository.saveProducts(listOf(product))
    
    // Act - Perform the action being tested
    repository.updateProductFrequency("1", 1234567900L)
    
    // Assert - Verify the expected outcome
    val products = repository.getProducts()
    assertEquals(6, products[0].frequency)
}
```

### Test Data Builders
```kotlin
// ✅ Good: Use builder functions for test data
object TestDataBuilder {
    fun createProduct(
        id: String = "1",
        name: String = "Test Product",
        frequency: Int = 0,
        lastPurchase: Long? = null,
        category: String = "Test"
    ) = Product(id, name, frequency, lastPurchase, category)
    
    fun createOrder(
        id: String = "order_1",
        items: List<OrderItem> = emptyList(),
        timestamp: Long = System.currentTimeMillis()
    ) = Order(id, items, timestamp)
}

// Usage in tests
@Test
fun testProductUpdate() {
    val product = TestDataBuilder.createProduct(frequency = 5)
    // ... test code
}
```

### Edge Cases to Test

```kotlin
class ProductRepositoryEdgeCaseTest {
    
    @Test
    fun `getProducts handles corrupted JSON gracefully`() = runBlocking {
        // Arrange
        val corruptedJson = "{corrupted json"
        writeToFile("products.json", corruptedJson)
        
        // Act
        val products = repository.getProducts()
        
        // Assert
        assertTrue(products.isEmpty())
    }
    
    @Test
    fun `updateProductFrequency handles non-existent product`() = runBlocking {
        // Act
        val result = repository.updateProductFrequency("non_existent_id", 123L)
        
        // Assert
        assertFalse(result)
    }
    
    @Test
    fun `processOrders handles network timeout`() = runBlocking {
        // Arrange
        val token = "timeout_test_token"
        mockServer.setResponseDelay(60_000) // 60 second delay
        
        // Act & Assert
        assertFailsWith<SocketTimeoutException> {
            repository.processOrders(token)
        }
    }
    
    @Test
    fun `processOrders handles empty order list`() = runBlocking {
        // Arrange
        mockServer.setOrderIds(emptyList())
        
        // Act
        repository.processOrders("test_token")
        
        // Assert - should complete without errors
        assertTrue(true)
    }
}
```

## 📊 Test Coverage Guidelines

### What to Test
- ✅ ViewModel state transitions
- ✅ Repository data operations
- ✅ JSON serialization/deserialization
- ✅ Business logic (frequency calculations, etc.)
- ✅ Error handling paths
- ✅ Edge cases (null values, empty lists, etc.)
- ✅ Data validation logic
- ✅ Coroutine cancellation handling

### What NOT to Test
- ❌ Android framework classes (don't test LiveData itself)
- ❌ Third-party library implementations (Gson, OkHttp internals)
- ❌ Simple getters/setters in data classes
- ❌ Generated code (View Binding classes)

## 🚀 Running Tests

### Command Line
```bash
# Run all unit tests
./gradlew test

# Run tests for a specific variant
./gradlew testDebugUnitTest

# Run instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest

# Run tests with coverage report
./gradlew jacocoTestReport
```

### Android Studio
- Right-click on test class/method → "Run"
- Run all tests in a directory: Right-click directory → "Run Tests"
- View coverage: Run → "Run with Coverage"

## 🔍 Mocking Guidelines

### When to Mock
- External dependencies (API clients, databases)
- Complex dependencies that are hard to set up
- Dependencies with side effects

### When NOT to Mock
- Simple data classes
- Pure functions
- Core business logic being tested

### Simple Mocking (No Framework)
```kotlin
// ✅ Good: Simple test implementation
class FakeProductRepository : ProductRepository {
    private val products = mutableListOf<Product>()
    
    override suspend fun getProducts(): List<Product> = products
    
    override suspend fun saveProducts(newProducts: List<Product>) {
        products.clear()
        products.addAll(newProducts)
    }
    
    fun addProduct(product: Product) {
        products.add(product)
    }
}
```

## 📝 Test Documentation

```kotlin
/**
 * Tests for ProductRepository focusing on data persistence and retrieval.
 * 
 * These tests verify:
 * - JSON file read/write operations
 * - Product frequency updates
 * - Error handling for file operations
 * - Data integrity across save/load cycles
 */
class ProductRepositoryTest {
    // Test implementation
}
```

## ✅ Testing Checklist

Before committing code, ensure:
- [ ] All new public methods have unit tests
- [ ] Edge cases are covered (null, empty, errors)
- [ ] Tests follow AAA pattern
- [ ] Test names are descriptive
- [ ] All tests pass locally
- [ ] No flaky tests (tests that randomly fail)
- [ ] Test code is clean and maintainable

## 🎓 Summary

- Write unit tests for ViewModels and Repositories
- Use Coroutines Test utilities for async code
- Follow AAA pattern for clear test structure
- Test edge cases and error conditions
- Use instrumented tests for Android-specific functionality
- Keep tests simple, focused, and maintainable
- Run tests frequently during development

Remember: **Good tests give you confidence to refactor and add features without breaking existing functionality.**
