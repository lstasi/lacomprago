# LaComprago - Authentication Flow

## Overview

LaComprago uses OAuth 2.0 for secure authentication with the supermarket API. This document details the complete authentication flow, token management, and security considerations.

## OAuth 2.0 Flow

### Authorization Code Flow

LaComprago implements the OAuth 2.0 Authorization Code flow, which is the most secure option for native mobile applications.

```
┌──────────┐                                           ┌──────────────┐
│          │                                           │              │
│  User    │                                           │  Auth        │
│          │                                           │  Server      │
└────┬─────┘                                           └──────┬───────┘
     │                                                        │
     │  1. Click "Login"                                     │
     ├──────────────────────────────────────────────────────►│
     │                                                        │
     │  2. Open Browser with Authorization URL               │
     │◄───────────────────────────────────────────────────────┤
     │     + client_id                                        │
     │     + redirect_uri                                     │
     │     + response_type=code                               │
     │     + scope                                            │
     │     + state                                            │
     │                                                        │
     │  3. User authenticates and grants permission          │
     ├──────────────────────────────────────────────────────►│
     │                                                        │
     │  4. Redirect to app with authorization code           │
     │◄───────────────────────────────────────────────────────┤
     │     redirect_uri?code=AUTH_CODE&state=STATE           │
     │                                                        │
┌────▼─────┐                                           ┌──────▼───────┐
│          │                                           │              │
│  App     │  5. Exchange code for tokens             │  API         │
│          ├──────────────────────────────────────────►│  Server      │
│          │     POST /oauth/token                     │              │
│          │     + code                                │              │
│          │     + client_id                           │              │
│          │     + client_secret (if required)         │              │
│          │                                           │              │
│          │  6. Return access & refresh tokens        │              │
│          │◄──────────────────────────────────────────┤              │
│          │                                           │              │
└──────────┘                                           └──────────────┘
```

## Authentication States

### State Machine

```kotlin
sealed class AuthState {
    object Unauthenticated : AuthState()
    object Authenticating : AuthState()
    data class Authenticated(
        val user: User,
        val expiresAt: Long
    ) : AuthState()
    data class TokenExpired(val canRefresh: Boolean) : AuthState()
    data class Error(val error: AuthError) : AuthState()
}

enum class AuthError {
    NETWORK_ERROR,
    INVALID_CREDENTIALS,
    TOKEN_EXPIRED,
    REFRESH_FAILED,
    CANCELLED_BY_USER,
    UNKNOWN
}
```

## Implementation Steps

### Step 1: Configure OAuth Client

```kotlin
object OAuthConfig {
    const val CLIENT_ID = "lacomprago_android_client"
    const val AUTHORIZATION_ENDPOINT = "https://auth.supermarket.com/oauth/authorize"
    const val TOKEN_ENDPOINT = "https://api.supermarket.com/oauth/token"
    const val REDIRECT_URI = "lacomprago://oauth/callback"
    
    val SCOPES = listOf(
        "orders:read",
        "cart:write",
        "profile:read"
    )
}
```

### Step 2: Build Authorization URL

```kotlin
fun buildAuthorizationUrl(state: String): String {
    val scopes = OAuthConfig.SCOPES.joinToString(" ")
    
    return buildString {
        append(OAuthConfig.AUTHORIZATION_ENDPOINT)
        append("?client_id=${OAuthConfig.CLIENT_ID}")
        append("&redirect_uri=${URLEncoder.encode(OAuthConfig.REDIRECT_URI, "UTF-8")}")
        append("&response_type=code")
        append("&scope=${URLEncoder.encode(scopes, "UTF-8")}")
        append("&state=$state")
    }
}
```

### Step 3: Launch Browser for Authentication

```kotlin
fun initiateOAuthFlow(context: Context) {
    val state = generateRandomState() // CSRF protection
    saveState(state) // Store for validation
    
    val authUrl = buildAuthorizationUrl(state)
    
    // Use Chrome Custom Tabs for better UX
    val builder = CustomTabsIntent.Builder()
    val customTabsIntent = builder.build()
    customTabsIntent.launchUrl(context, Uri.parse(authUrl))
}

private fun generateRandomState(): String {
    return UUID.randomUUID().toString()
}
```

### Step 4: Handle OAuth Callback

**AndroidManifest.xml**
```xml
<activity
    android:name=".auth.OAuthCallbackActivity"
    android:launchMode="singleTop"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data
            android:scheme="lacomprago"
            android:host="oauth"
            android:path="/callback" />
    </intent-filter>
</activity>
```

**OAuthCallbackActivity.kt**
```kotlin
class OAuthCallbackActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val uri = intent.data
        if (uri != null && uri.scheme == "lacomprago") {
            handleOAuthCallback(uri)
        } else {
            handleError(AuthError.INVALID_CALLBACK)
        }
        
        finish()
    }
    
    private fun handleOAuthCallback(uri: Uri) {
        val code = uri.getQueryParameter("code")
        val state = uri.getQueryParameter("state")
        val error = uri.getQueryParameter("error")
        
        when {
            error != null -> handleError(error)
            code != null && state != null -> {
                if (validateState(state)) {
                    exchangeCodeForToken(code)
                } else {
                    handleError(AuthError.INVALID_STATE)
                }
            }
            else -> handleError(AuthError.INVALID_CALLBACK)
        }
    }
}
```

### Step 5: Exchange Authorization Code for Token

```kotlin
suspend fun exchangeCodeForToken(code: String): Result<AuthToken> {
    return withContext(Dispatchers.IO) {
        try {
            val request = TokenRequest(
                grant_type = "authorization_code",
                code = code,
                client_id = OAuthConfig.CLIENT_ID,
                redirect_uri = OAuthConfig.REDIRECT_URI
            )
            
            val response = apiService.exchangeToken(request)
            
            val token = AuthToken(
                accessToken = response.access_token,
                refreshToken = response.refresh_token,
                tokenType = response.token_type,
                expiresIn = response.expires_in,
                expiresAt = System.currentTimeMillis() + (response.expires_in * 1000),
                scope = response.scope
            )
            
            // Store token securely
            tokenManager.saveToken(token)
            
            Result.success(token)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

## Token Management

### Secure Token Storage

**EncryptedSharedPreferences**
```kotlin
class SecureTokenStorage(context: Context) {
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        "auth_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    fun saveToken(token: AuthToken) {
        encryptedPrefs.edit {
            putString("access_token", token.accessToken)
            putString("refresh_token", token.refreshToken)
            putString("token_type", token.tokenType)
            putLong("expires_at", token.expiresAt)
            putString("scope", token.scope)
        }
    }
    
    fun getToken(): AuthToken? {
        val accessToken = encryptedPrefs.getString("access_token", null)
            ?: return null
        
        return AuthToken(
            accessToken = accessToken,
            refreshToken = encryptedPrefs.getString("refresh_token", null),
            tokenType = encryptedPrefs.getString("token_type", "Bearer")!!,
            expiresIn = 0L, // Not stored
            expiresAt = encryptedPrefs.getLong("expires_at", 0L),
            scope = encryptedPrefs.getString("scope", null)
        )
    }
    
    fun clearToken() {
        encryptedPrefs.edit {
            clear()
        }
    }
}
```

### Token Refresh

```kotlin
class TokenManager(
    private val apiService: ApiService,
    private val tokenStorage: SecureTokenStorage
) {
    
    private val refreshLock = Mutex()
    
    suspend fun getValidToken(): String? {
        val token = tokenStorage.getToken() ?: return null
        
        return when {
            isTokenValid(token) -> token.accessToken
            canRefreshToken(token) -> refreshToken(token)
            else -> null
        }
    }
    
    private fun isTokenValid(token: AuthToken): Boolean {
        val now = System.currentTimeMillis()
        val bufferTime = 5 * 60 * 1000 // 5 minutes buffer
        return token.expiresAt > (now + bufferTime)
    }
    
    private fun canRefreshToken(token: AuthToken): Boolean {
        return token.refreshToken != null
    }
    
    private suspend fun refreshToken(currentToken: AuthToken): String? {
        refreshLock.withLock {
            // Check again in case another coroutine already refreshed
            val latestToken = tokenStorage.getToken()
            if (latestToken != null && 
                latestToken.accessToken != currentToken.accessToken) {
                return latestToken.accessToken
            }
            
            try {
                val request = TokenRequest(
                    grant_type = "refresh_token",
                    refresh_token = currentToken.refreshToken,
                    client_id = OAuthConfig.CLIENT_ID
                )
                
                val response = apiService.refreshToken(request)
                
                val newToken = AuthToken(
                    accessToken = response.access_token,
                    refreshToken = response.refresh_token 
                        ?: currentToken.refreshToken,
                    tokenType = response.token_type,
                    expiresIn = response.expires_in,
                    expiresAt = System.currentTimeMillis() + 
                        (response.expires_in * 1000),
                    scope = response.scope
                )
                
                tokenStorage.saveToken(newToken)
                
                return newToken.accessToken
            } catch (e: Exception) {
                // Refresh failed, clear token and require re-login
                tokenStorage.clearToken()
                return null
            }
        }
    }
}
```

### Automatic Token Refresh

**AuthInterceptor with Auto-Refresh**
```kotlin
class AuthInterceptor(
    private val tokenManager: TokenManager
) : Interceptor {
    
    override fun intercept(chain: Chain): Response {
        val original = chain.request()
        
        // Skip authentication for token endpoint
        if (original.url.encodedPath.contains("/oauth/token")) {
            return chain.proceed(original)
        }
        
        // Get valid token (refreshes if needed)
        val token = runBlocking {
            tokenManager.getValidToken()
        }
        
        val request = if (token != null) {
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }
        
        val response = chain.proceed(request)
        
        // Handle 401 (token might have expired during request)
        if (response.code == 401) {
            response.close()
            
            // Try to refresh token
            val newToken = runBlocking {
                tokenManager.refreshToken()
            }
            
            if (newToken != null) {
                // Retry request with new token
                return chain.proceed(
                    original.newBuilder()
                        .header("Authorization", "Bearer $newToken")
                        .build()
                )
            } else {
                // Refresh failed, user needs to re-login
                // Emit event to trigger logout flow
            }
        }
        
        return response
    }
}
```

## Logout Flow

```kotlin
suspend fun logout() {
    withContext(Dispatchers.IO) {
        try {
            // Optional: Revoke token on server
            val token = tokenStorage.getToken()
            if (token != null) {
                revokeToken(token.accessToken)
            }
        } catch (e: Exception) {
            // Log error but continue with local cleanup
        } finally {
            // Clear local token
            tokenStorage.clearToken()
            
            // Clear any cached data
            clearUserData()
            
            // Navigate to login screen
            navigateToLogin()
        }
    }
}

private suspend fun revokeToken(token: String) {
    // Call revocation endpoint if available
    apiService.revokeToken(token)
}
```

## Security Considerations

### 1. CSRF Protection

**State Parameter**
- Generate random state for each auth request
- Validate state on callback
- Use cryptographically secure random generation

```kotlin
fun generateState(): String {
    val random = SecureRandom()
    val bytes = ByteArray(32)
    random.nextBytes(bytes)
    return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP)
}
```

### 2. PKCE (Proof Key for Code Exchange)

For enhanced security (optional but recommended):

```kotlin
// Generate code verifier
fun generateCodeVerifier(): String {
    val random = SecureRandom()
    val bytes = ByteArray(32)
    random.nextBytes(bytes)
    return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
}

// Generate code challenge
fun generateCodeChallenge(verifier: String): String {
    val bytes = verifier.toByteArray(Charsets.US_ASCII)
    val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
    return Base64.encodeToString(digest, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
}

// Add to authorization URL
fun buildAuthorizationUrl(state: String, codeChallenge: String): String {
    // ... existing parameters
    append("&code_challenge=$codeChallenge")
    append("&code_challenge_method=S256")
}

// Include verifier in token exchange
fun exchangeCodeForToken(code: String, codeVerifier: String): Result<AuthToken> {
    val request = TokenRequest(
        grant_type = "authorization_code",
        code = code,
        client_id = OAuthConfig.CLIENT_ID,
        redirect_uri = OAuthConfig.REDIRECT_URI,
        code_verifier = codeVerifier
    )
    // ... rest of implementation
}
```

### 3. Token Security

**Best Practices**
- Never log tokens
- Store tokens encrypted
- Clear tokens on logout
- Use HTTPS only
- Implement token expiration
- Rotate refresh tokens

### 4. Secure Communication

**SSL/TLS**
- Use HTTPS for all requests
- Certificate pinning (optional)
- Validate SSL certificates

## Error Handling

### Authentication Errors

```kotlin
sealed class AuthError {
    object NetworkError : AuthError()
    object InvalidCredentials : AuthError()
    object TokenExpired : AuthError()
    object RefreshFailed : AuthError()
    object UserCancelled : AuthError()
    data class ServerError(val code: Int, val message: String) : AuthError()
    object Unknown : AuthError()
}

fun handleAuthError(error: AuthError) {
    when (error) {
        is AuthError.NetworkError -> {
            showMessage("No internet connection")
        }
        is AuthError.InvalidCredentials -> {
            showMessage("Invalid credentials. Please try again.")
        }
        is AuthError.TokenExpired -> {
            // Attempt automatic refresh
            attemptTokenRefresh()
        }
        is AuthError.RefreshFailed -> {
            showMessage("Session expired. Please login again.")
            navigateToLogin()
        }
        is AuthError.UserCancelled -> {
            // User cancelled authentication
        }
        is AuthError.ServerError -> {
            showMessage("Server error: ${error.message}")
        }
        is AuthError.Unknown -> {
            showMessage("An unexpected error occurred")
        }
    }
}
```

## Testing

### Unit Tests

```kotlin
@Test
fun `test token expiration check`() {
    val expiredToken = AuthToken(
        accessToken = "token",
        refreshToken = "refresh",
        tokenType = "Bearer",
        expiresIn = 3600,
        expiresAt = System.currentTimeMillis() - 1000,
        scope = null
    )
    
    assertFalse(tokenManager.isTokenValid(expiredToken))
}

@Test
fun `test successful token refresh`() = runTest {
    val oldToken = createTestToken()
    
    coEvery { apiService.refreshToken(any()) } returns TokenResponse(
        access_token = "new_token",
        refresh_token = "new_refresh",
        token_type = "Bearer",
        expires_in = 3600
    )
    
    val newToken = tokenManager.refreshToken(oldToken)
    
    assertNotNull(newToken)
    assertEquals("new_token", newToken)
}
```

### Integration Tests

```kotlin
@Test
fun `test complete OAuth flow`() {
    // 1. Build authorization URL
    val authUrl = buildAuthorizationUrl("state123")
    assertTrue(authUrl.contains("client_id"))
    
    // 2. Simulate callback
    val callbackUri = Uri.parse("lacomprago://oauth/callback?code=CODE&state=state123")
    
    // 3. Exchange code for token
    val token = exchangeCodeForToken("CODE")
    
    // 4. Verify token is stored
    val storedToken = tokenStorage.getToken()
    assertNotNull(storedToken)
}
```

## User Experience

### Loading States

**During Authentication**
- Show loading indicator
- Disable interaction
- Provide cancel option

**On Success**
- Navigate to main screen
- Show welcome message

**On Error**
- Clear loading state
- Show error message
- Provide retry option

### Session Management

**App Launch**
- Check for valid token
- Auto-login if valid
- Show login screen if invalid

**Background/Foreground**
- Maintain session
- Refresh token if needed
- Handle token expiration gracefully

## Conclusion

This authentication flow provides secure, user-friendly OAuth 2.0 implementation with proper token management, automatic refresh, and comprehensive error handling for the LaComprago application.
