package com.securedocs.app.security

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import java.security.MessageDigest

object SignatureValidator {

    private const val TAG = "SignatureValidator"

    /**
     * Replace this with the SHA-256 of your release keystore signature.
     * Get it by running:
     *   keytool -printcert -jarfile your-release.apk
     * or from Play Console → Release → App Integrity.
     *
     * Leave empty ("") during development — validation is skipped when empty.
     */
    private const val EXPECTED_SHA256 = ""

    fun isSignatureValid(context: Context): Boolean {
        if (EXPECTED_SHA256.isEmpty()) return true

        val actual = getSignatureSha256(context) ?: return false
        val valid  = actual.equals(EXPECTED_SHA256, ignoreCase = true)

        if (!valid) {
            Log.e(TAG, "Signature mismatch! Expected: $EXPECTED_SHA256 | Got: $actual")
        }
        return valid
    }

    fun getSignatureSha256(context: Context): String? {
        return try {
            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val info = context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
                info.signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                val info = context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNATURES
                )
                @Suppress("DEPRECATION")
                info.signatures
            }

            if (signatures.isNullOrEmpty()) return null

            val md    = MessageDigest.getInstance("SHA-256")
            val bytes = md.digest(signatures[0].toByteArray())
            bytes.joinToString(":") { "%02X".format(it) }
        } catch (e: Exception) {
            Log.e(TAG, "getSignatureSha256 failed: ${e.message}")
            null
        }
    }
}
