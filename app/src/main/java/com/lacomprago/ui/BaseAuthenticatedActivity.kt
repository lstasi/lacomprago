package com.lacomprago.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.lacomprago.storage.TokenStorage

/**
 * Base activity that provides authentication-aware behavior.
 * Activities that require an authenticated user should extend this class
 * to get shared logout functionality and access to token storage.
 */
abstract class BaseAuthenticatedActivity : AppCompatActivity() {

    /**
     * Lazily initialized token storage, shared across the activity lifecycle.
     * Subclasses may use this to read/write tokens without creating separate instances.
     */
    protected val tokenStorage: TokenStorage by lazy {
        TokenStorage(applicationContext)
    }

    /**
     * Clear the stored token and navigate back to the login screen.
     * Clears the entire activity back stack so the user starts fresh.
     */
    protected fun logout() {
        tokenStorage.clearToken()
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}
