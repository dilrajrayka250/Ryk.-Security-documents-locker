package com.securedocs.app.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * Runtime permission helpers — handles API level differences automatically.
 * All launcher-based permission requests use Activity Result API (non-deprecated).
 */
object PermissionHelper {

    // ── Camera ────────────────────────────────────────────────────────────────

    fun hasCameraPermission(activity: Activity): Boolean =
        ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED

    // ── Storage (API-aware) ───────────────────────────────────────────────────

    fun hasStoragePermission(activity: Activity): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+: READ_MEDIA_IMAGES
            ContextCompat.checkSelfPermission(
                activity, Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Android 12 and below
            ContextCompat.checkSelfPermission(
                activity, Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Returns the correct permission string for the current API level.
     * Pass this to [registerForActivityResult] → [RequestPermission].
     */
    fun getStoragePermission(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }
}
