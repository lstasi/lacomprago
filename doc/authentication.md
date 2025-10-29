# LaCompraGo - Authentication Flow

## Overview

LaCompraGo uses simple token-based authentication. Users input their API token through a text field, and the token is stored securely for subsequent API calls.

## Authentication Approach

### Simple Token Input

Unlike complex OAuth flows, LaCompraGo uses a straightforward approach:
- User pastes or types their API token
- Token is validated on first API call
- Token is stored encrypted locally
- No login screens or OAuth redirects

## Authentication Flow

```
┌──────────┐
│          │
│  User    │
│          │
└────┬─────┘
     │
     │  1. Open app
     │
     ↓
┌──────────┐
│  Token   │
│  Input   │  User pastes/types API token
│  Screen  │
└────┬─────┘
     │
     │  2. Token entered
     │
     ↓
┌──────────┐
│   App    │  3. Store token encrypted
│          │  4. Validate with API call
└────┬─────┘
     │
     │  5. Token valid → proceed
     │     Token invalid → show error, retry
     │
     ↓
┌──────────┐
│ Product  │
│   List   │
│  Screen  │
└──────────┘
```

## Authentication States

### State Machine

```kotlin
sealed class AuthState {
    object NoToken : AuthState()
    object ValidatingToken : AuthState()
    data class TokenValid(val token: String) : AuthState()
    data class TokenInvalid(val error: String) : AuthState()
}
```

## Implementation

### Token Input Screen

**UI Components**
- Text field for token input
- Submit button
- Error message display
- Optional: Info text about where to get token

```kotlin
// Simple UI layout
TextField(
    value = tokenInput,
    onValueChange = { tokenInput = it },
    label = "API Token",
    singleLine = true
)

Button(onClick = { submitToken() }) {
    Text("Submit")
}

if (errorMessage != null) {
    Text(errorMessage, color = Color.Red)
}
```

### Token Storage

**EncryptedSharedPreferences**

```kotlin
class TokenStorage(context: Context) {
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        "token_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    fun saveToken(token: String) {
        encryptedPrefs.edit {
            putString("api_token", token)
            putLong("stored_at", System.currentTimeMillis())
        }
    }
    
    fun getToken(): String? {
        return encryptedPrefs.getString("api_token", null)
    }
    
    fun clearToken() {
        encryptedPrefs.edit {
            remove("api_token")
            remove("stored_at")
        }
    }
    
    fun hasToken(): Boolean {
        return getToken() != null
    }
}
```

### Token Validation

**Validate on First API Call**

```kotlin
class TokenValidator(
    private val apiClient: ApiClient,
    private val tokenStorage: TokenStorage
) {
    suspend fun validateToken(token: String): Result<Boolean> {
        return try {
            // Make a simple API call to validate token
            val response = apiClient.validateToken(token)
            
            if (response.isSuccessful) {
                // Token is valid, store it
                tokenStorage.saveToken(token)
                Result.success(true)
            } else {
                // Token is invalid
                Result.failure(Exception("Invalid token"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### ViewModel Implementation

```kotlin
class AuthViewModel(
    private val tokenStorage: TokenStorage,
    private val tokenValidator: TokenValidator
) : ViewModel() {
    
    private val _authState = MutableLiveData<AuthState>(AuthState.NoToken)
    val authState: LiveData<AuthState> = _authState
    
    init {
        checkExistingToken()
    }
    
    private fun checkExistingToken() {
        val token = tokenStorage.getToken()
        if (token != null) {
            _authState.value = AuthState.TokenValid(token)
        } else {
            _authState.value = AuthState.NoToken
        }
    }
    
    fun submitToken(token: String) {
        if (token.isBlank()) {
            _authState.value = AuthState.TokenInvalid("Token cannot be empty")
            return
        }
        
        _authState.value = AuthState.ValidatingToken
        
        viewModelScope.launch {
            val result = tokenValidator.validateToken(token)
            
            _authState.value = if (result.isSuccess) {
                AuthState.TokenValid(token)
            } else {
                AuthState.TokenInvalid(
                    result.exceptionOrNull()?.message ?: "Invalid token"
                )
            }
        }
    }
    
    fun clearToken() {
        tokenStorage.clearToken()
        _authState.value = AuthState.NoToken
    }
}
```

## API Integration

### Adding Token to Requests

**Simple Interceptor**

```kotlin
class TokenInterceptor(
    private val tokenStorage: TokenStorage
) : Interceptor {
    
    override fun intercept(chain: Chain): Response {
        val original = chain.request()
        val token = tokenStorage.getToken()
        
        val request = if (token != null) {
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }
        
        val response = chain.proceed(request)
        
        // Handle 401 Unauthorized
        if (response.code == 401) {
            // Token is invalid, clear it
            tokenStorage.clearToken()
        }
        
        return response
    }
}
```

### OkHttp Client Setup

```kotlin
fun createApiClient(tokenStorage: TokenStorage): OkHttpClient {
    return OkHttpClient.Builder()
        .addInterceptor(TokenInterceptor(tokenStorage))
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
}
```

## Security Considerations

### Token Security

**Best Practices**
- Store token encrypted using EncryptedSharedPreferences
- Never log the token value
- Clear token on logout
- Use HTTPS for all API calls
- Handle 401 errors by prompting for new token

### Secure Storage

**EncryptedSharedPreferences Benefits**
- AES256-GCM encryption
- Keys stored in Android Keystore
- Automatic key generation
- Built into AndroidX Security library

### Token Validation

**Validation Strategy**
- Validate token on first API call
- Don't store invalid tokens
- Clear token if API returns 401
- Re-prompt user for valid token

## Error Handling

### Common Errors

```kotlin
sealed class TokenError {
    object EmptyToken : TokenError()
    object InvalidFormat : TokenError()
    object NetworkError : TokenError()
    object Unauthorized : TokenError()
    data class Unknown(val message: String) : TokenError()
}

fun handleTokenError(error: TokenError): String {
    return when (error) {
        is TokenError.EmptyToken -> "Please enter a token"
        is TokenError.InvalidFormat -> "Token format is invalid"
        is TokenError.NetworkError -> "Network error. Please try again"
        is TokenError.Unauthorized -> "Token is invalid or expired"
        is TokenError.Unknown -> "Error: ${error.message}"
    }
}
```

### Error Recovery

**User Actions**
- Show clear error message
- Allow user to re-enter token
- Provide help text or link to get token
- Option to clear stored token and start over

## User Experience

### App Launch Flow

```kotlin
fun determineInitialScreen(): Screen {
    return if (tokenStorage.hasToken()) {
        Screen.ProductList
    } else {
        Screen.TokenInput
    }
}
```

### Token Input Screen Design

**Minimal UI**
```
┌─────────────────────────────────┐
│  LaCompraGo                     │
├─────────────────────────────────┤
│                                 │
│  Enter your API token:          │
│                                 │
│  ┌───────────────────────────┐  │
│  │ [token text field]        │  │
│  └───────────────────────────┘  │
│                                 │
│  [Submit Button]                │
│                                 │
│  Where to get your token?       │
│  [Help Link]                    │
│                                 │
└─────────────────────────────────┘
```

### Loading State

While validating token:
- Show progress indicator
- Disable input
- Display "Validating token..." message

### Success State

When token is valid:
- Show brief success message
- Navigate to product list screen
- Store token for future sessions

### Error State

When token is invalid:
- Show error message
- Keep token input visible
- Allow user to try again
- Provide clear guidance

## Token Management

### Logout

```kotlin
fun logout() {
    // Clear stored token
    tokenStorage.clearToken()
    
    // Navigate to token input screen
    navigateToTokenInput()
}
```

### Token Expiration

**Handling 401 Errors**

```kotlin
fun handleUnauthorized() {
    // Clear invalid token
    tokenStorage.clearToken()
    
    // Show message
    showMessage("Token expired or invalid. Please enter a new token.")
    
    // Navigate to token input
    navigateToTokenInput()
}
```

## Testing

### Unit Tests

```kotlin
@Test
fun `test token storage and retrieval`() {
    val token = "test_token_123"
    
    tokenStorage.saveToken(token)
    
    val retrieved = tokenStorage.getToken()
    assertEquals(token, retrieved)
}

@Test
fun `test token validation success`() = runTest {
    val validToken = "valid_token"
    
    coEvery { apiClient.validateToken(validToken) } returns 
        Response.success(Unit)
    
    val result = tokenValidator.validateToken(validToken)
    
    assertTrue(result.isSuccess)
}

@Test
fun `test token validation failure`() = runTest {
    val invalidToken = "invalid_token"
    
    coEvery { apiClient.validateToken(invalidToken) } returns 
        Response.error(401, mockErrorBody)
    
    val result = tokenValidator.validateToken(invalidToken)
    
    assertTrue(result.isFailure)
}
```

### Integration Tests

```kotlin
@Test
fun `test complete token flow`() {
    // 1. Start with no token
    assertFalse(tokenStorage.hasToken())
    
    // 2. User enters token
    viewModel.submitToken("test_token")
    
    // 3. Verify token is validated
    assertEquals(AuthState.ValidatingToken, viewModel.authState.value)
    
    // 4. Wait for validation
    advanceUntilIdle()
    
    // 5. Verify token is stored
    assertTrue(tokenStorage.hasToken())
}
```

## Configuration

### API Endpoints

```kotlin
object ApiConfig {
    const val BASE_URL = "https://api.supermarket.example.com/"
    const val TOKEN_VALIDATION_ENDPOINT = "api/validate"
}
```

### Token Format

**Expected Format**
- Bearer token
- Alphanumeric string
- Variable length (typically 32-64 characters)
- No specific format validation (validated by API)

## Comparison with OAuth

### Why Simple Token Input?

**Advantages**
- Much simpler implementation
- No browser redirects
- No complex OAuth flow
- Minimal dependencies
- Easier to test
- Better for simple apps

**Trade-offs**
- User must obtain token externally
- No automated token refresh
- User responsible for token security
- Simpler but less automated

## Conclusion

This simple token-based authentication approach provides secure, straightforward access to the API while minimizing complexity and dependencies. It's ideal for LaCompraGo's use case where simplicity is prioritized.
