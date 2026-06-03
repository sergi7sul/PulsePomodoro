package com.cherrytime.data.remote

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiKeyStore @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val prefs: SharedPreferences by lazy {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        EncryptedSharedPreferences.create(
            "gemini_secure_prefs",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    fun getKey(): String? = prefs.getString(KEY_GEMINI_API, null)?.takeIf { it.isNotBlank() }

    fun setKey(key: String) = prefs.edit().putString(KEY_GEMINI_API, key.trim()).apply()

    fun clearKey() = prefs.edit().remove(KEY_GEMINI_API).apply()

    companion object {
        private const val KEY_GEMINI_API = "gemini_api_key"
    }
}
