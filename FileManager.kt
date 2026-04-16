package com.securedocs.app.storage

import android.content.Context
import android.net.Uri
import android.util.Log
import com.securedocs.app.security.EncryptionHelper
import java.io.File
import java.io.FileOutputStream

/**
 * Manages all file I/O for normal and private (AES-256 encrypted) documents.
 * All operations are exception-safe — returns false/null instead of crashing.
 */
object FileManager {

    private const val TAG         = "FileManager"
    private const val DIR_NORMAL  = "normal_docs"
    private const val DIR_PRIVATE = "private_docs"
    private const val DIR_BACKUP  = "backup"
    private const val ENC_SUFFIX  = ".enc"

    // ── Folder helpers ────────────────────────────────────────────────────────

    private fun getDir(context: Context, name: String): File =
        File(context.filesDir, name).also { if (!it.exists()) it.mkdirs() }

    fun normalDir(context: Context)  = getDir(context, DIR_NORMAL)
    fun privateDir(context: Context) = getDir(context, DIR_PRIVATE)
    fun backupDir(context: Context)  = getDir(context, DIR_BACKUP)

    // ── Save normal file ──────────────────────────────────────────────────────

    fun saveNormalFile(context: Context, uri: Uri, fileName: String): Boolean {
        return try {
            val input = context.contentResolver.openInputStream(uri) ?: return false
            val dest  = File(normalDir(context), sanitizeName(fileName))
            FileOutputStream(dest).use { out -> input.copyTo(out) }
            input.close()
            Log.d(TAG, "Normal file saved: ${dest.name}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "saveNormalFile failed: ${e.message}")
            false
        }
    }

    // ── Save QR scan result ───────────────────────────────────────────────────

    fun saveQrResult(context: Context, text: String): Boolean {
        return try {
            val name = "qr_${System.currentTimeMillis()}.txt"
            File(normalDir(context), name).writeText(text)
            Log.d(TAG, "QR result saved: $name")
            true
        } catch (e: Exception) {
            Log.e(TAG, "saveQrResult failed: ${e.message}")
            false
        }
    }

    // ── Save encrypted (private) file ─────────────────────────────────────────

    fun savePrivateFile(context: Context, uri: Uri, fileName: String): Boolean {
        return try {
            val input = context.contentResolver.openInputStream(uri) ?: return false
            val raw   = input.readBytes()
            input.close()
            val enc  = EncryptionHelper.encrypt(raw)
            val dest = File(privateDir(context), "${sanitizeName(fileName)}$ENC_SUFFIX")
            FileOutputStream(dest).use { it.write(enc) }
            Log.d(TAG, "Private file encrypted & saved: ${dest.name}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "savePrivateFile failed: ${e.message}")
            false
        }
    }

    // ── Decrypt a private file ────────────────────────────────────────────────

    fun decryptPrivateFile(file: File): ByteArray? {
        return try {
            EncryptionHelper.decrypt(file.readBytes())
        } catch (e: Exception) {
            Log.e(TAG, "decryptPrivateFile failed: ${e.message}")
            null
        }
    }

    // ── List files ────────────────────────────────────────────────────────────

    fun listNormalFiles(context: Context): List<File> =
        normalDir(context).listFiles()
            ?.filter { it.isFile }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()

    fun listPrivateFiles(context: Context): List<File> =
        privateDir(context).listFiles()
            ?.filter { it.isFile }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()

    // ── Delete ────────────────────────────────────────────────────────────────

    fun delete(file: File): Boolean {
        return try {
            file.delete().also { Log.d(TAG, "Deleted: ${file.name} — $it") }
        } catch (e: Exception) {
            Log.e(TAG, "delete failed: ${e.message}")
            false
        }
    }

    // ── Backup all files ──────────────────────────────────────────────────────

    fun backupAll(context: Context): Int {
        var count = 0
        val dest  = backupDir(context)
        return try {
            (listNormalFiles(context) + listPrivateFiles(context)).forEach { f ->
                try {
                    f.copyTo(File(dest, f.name), overwrite = true)
                    count++
                } catch (e: Exception) {
                    Log.e(TAG, "Backup failed for ${f.name}: ${e.message}")
                }
            }
            Log.d(TAG, "Backup complete: $count files")
            count
        } catch (e: Exception) {
            Log.e(TAG, "backupAll failed: ${e.message}")
            count
        }
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    fun readableSize(bytes: Long): String = when {
        bytes < 1_024         -> "$bytes B"
        bytes < 1_048_576     -> "${bytes / 1_024} KB"
        bytes < 1_073_741_824 -> "${bytes / 1_048_576} MB"
        else                  -> "${bytes / 1_073_741_824} GB"
    }

    fun displayName(file: File): String = file.name.removeSuffix(ENC_SUFFIX)

    private fun sanitizeName(name: String): String =
        name.replace(Regex("[\\\\/:*?\"<>|]"), "_").take(200)
}
