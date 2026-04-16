package com.securedocs.app.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Encrypted SharedPreferences wrapper for secure key-value storage.
 * Call [init] once in Application.onCreate().
 */
object Prefs {

    private const val PREF_FILE   = "secure_docs_prefs"
    private const val KEY_PIN     = "user_pin"
    private const val KEY_PIN_SET = "pin_set"
    private const val KEY_PREMIUM = "is_premium"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                context,
                PREF_FILE,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Fallback for edge-case device issues
            context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
        }
    }

    // ── PIN ──────────────────────────────────────────────────────────────────

    fun isPinSet(): Boolean = prefs.getBoolean(KEY_PIN_SET, false)

    fun savePin(pin: String) {
        prefs.edit()
            .putString(KEY_PIN, pin)
            .putBoolean(KEY_PIN_SET, true)
            .apply()
    }

    fun verifyPin(input: String): Boolean = prefs.getString(KEY_PIN, "") == input

    // ── Premium ───────────────────────────────────────────────────────────────

    fun isPremium(): Boolean = prefs.getBoolean(KEY_PREMIUM, false)

    fun setPremium(value: Boolean) {
        prefs.edit().putBoolean(KEY_PREMIUM, value).apply()
    }
}
