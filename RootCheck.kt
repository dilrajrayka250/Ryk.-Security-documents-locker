package com.securedocs.app.security

import android.os.Build
import java.io.File

object RootCheck {

    private val ROOT_PATHS = arrayOf(
        "/system/app/Superuser.apk",
        "/system/xbin/su",
        "/system/bin/su",
        "/sbin/su",
        "/data/local/su",
        "/data/local/xbin/su",
        "/data/local/bin/su",
        "/system/sd/xbin/su",
        "/system/bin/failsafe/su",
        "/data/local/tmp/su",
        "/system/app/SuperSU.apk",
        "/system/app/SuperSU/SuperSU.apk",
        "/system/app/Kinguser.apk",
        "/data/adb/magisk",
        "/sbin/.magisk",
        "/sbin/.core/mirror",
        "/sbin/.core/img"
    )

    fun isDeviceRooted(): Boolean {
        return checkTestKeys() || checkRootPaths() || checkSuCommand()
    }

    private fun checkTestKeys(): Boolean {
        val tags = Build.TAGS
        return tags != null && tags.contains("test-keys")
    }

    private fun checkRootPaths(): Boolean {
        return ROOT_PATHS.any { File(it).exists() }
    }

    private fun checkSuCommand(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
            val result  = process.inputStream.read()
            process.destroy()
            result != -1
        } catch (e: Exception) {
            false
        }
    }
}
