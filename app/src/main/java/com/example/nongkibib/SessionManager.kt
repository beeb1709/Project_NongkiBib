package com.example.nongkibib

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.example.nongkibib.model.User
import com.google.gson.Gson

/**
 * SessionManager handles secure storage of user data using EncryptedSharedPreferences.
 */
class SessionManager(context: Context) {
    
    // Create or retrieve the Master Key for encryption
    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    // Initialize EncryptedSharedPreferences
    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        "NongkiBibSecurePrefs",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    private val gson = Gson()

    companion object {
        private const val KEY_USER = "user_session"
        private const val KEY_TOKEN = "auth_token"
    }

    fun saveUser(user: User) {
        val userJson = gson.toJson(user)
        prefs.edit().putString(KEY_USER, userJson).apply()
    }

    fun getUser(): User? {
        val userJson = prefs.getString(KEY_USER, null)
        return if (userJson != null) {
            try {
                gson.fromJson(userJson, User::class.java)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    fun isLoggedIn(): Boolean {
        return getUser() != null && getToken() != null
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
