package com.lacomprago.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Secure storage for API token using EncryptedSharedPreferences.
 * Token is encrypted at rest using AES256-GCM.
 */
class TokenStorage(context: Context) {
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val encryptedPrefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_FILE_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    /**
     * Save the API token securely.
     * @param token The API token to store
     */
    fun saveToken(token: String) {
        encryptedPrefs.edit {
            putString(KEY_TOKEN, token)
            putLong(KEY_STORED_AT, System.currentTimeMillis())
        }
    }
    
    /**
     * Retrieve the stored API token.
     * @return The stored token, or null if no token exists
     */
    fun getToken(): String? {
        return encryptedPrefs.getString(KEY_TOKEN, null)
    }
    
    /**
     * Check if a token is stored.
     * @return true if a token exists, false otherwise
     */
    fun hasToken(): Boolean {
        return getToken() != null
    }
    
    /**
     * Clear the stored token.
     */
    fun clearToken() {
        encryptedPrefs.edit {
            remove(KEY_TOKEN)
            remove(KEY_STORED_AT)
        }
    }
    
    /**
     * Get the timestamp when the token was stored.
     * @return Timestamp in milliseconds, or null if no token exists
     */
    fun getStoredTimestamp(): Long? {
        val timestamp = encryptedPrefs.getLong(KEY_STORED_AT, -1L)
        return if (timestamp == -1L) null else timestamp
    }
    
    companion object {
        private const val PREFS_FILE_NAME = "token_prefs"
        private const val KEY_TOKEN = "api_token"
        private const val KEY_STORED_AT = "stored_at"
    }
}
