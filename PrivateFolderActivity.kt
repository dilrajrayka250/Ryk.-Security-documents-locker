package com.securedocs.app.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.securedocs.app.R
import com.securedocs.app.databinding.ActivityPrivateBinding
import com.securedocs.app.security.EncryptionHelper
import com.securedocs.app.security.RootCheck
import com.securedocs.app.storage.FileManager
import java.io.File

class PrivateFolderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrivateBinding
    private val files = mutableListOf<File>()
    private var authenticated = false

    private val filePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri -> handlePickedFile(uri) }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        binding = ActivityPrivateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (RootCheck.isDeviceRooted()) {
            showRootWarningAndExit()
            return
        }

        EncryptionHelper.generateKeyIfNeeded()
        setupRecyclerView()
        promptBiometric()

        binding.fabAddPrivate.setOnClickListener {
            if (authenticated) openPicker() else promptBiometric()
        }
    }

    private fun showRootWarningAndExit() {
        AlertDialog.Builder(this)
            .setTitle("Security Warning")
            .setMessage(
                "Rooted device detected.\n\n" +
                "Private folder is disabled on rooted devices to protect your encrypted documents."
            )
            .setPositiveButton("OK") { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }

    private fun promptBiometric() {
        val canAuth = BiometricManager.from(this).canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
        if (canAuth != BiometricManager.BIOMETRIC_SUCCESS) {
            authenticated = true
            loadFiles()
            return
        }

        BiometricPrompt(
            this,
            ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    authenticated = true
                    loadFiles()
                }
                override fun onAuthenticationError(code: Int, msg: CharSequence) {
                    Toast.makeText(this@PrivateFolderActivity, "Auth error: $msg", Toast.LENGTH_SHORT).show()
                    finish()
                }
                override fun onAuthenticationFailed() {
                    Toast.makeText(this@PrivateFolderActivity, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            }
        ).authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("Private Folder")
                .setSubtitle("AES-256 Encrypted")
                .setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_STRONG or
                            BiometricManager.Authenticators.DEVICE_CREDENTIAL
                )
                .build()
        )
    }

    private fun setupRecyclerView() {
        binding.rvPrivateFiles.layoutManager = LinearLayoutManager(this)
        binding.rvPrivateFiles.adapter = PrivateAdapter(files) { file ->
            FileManager.delete(file)
            Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show()
            loadFiles()
        }
    }

    private fun loadFiles() {
        files.clear()
        files.addAll(FileManager.listPrivateFiles(this))
        binding.rvPrivateFiles.adapter?.notifyDataSetChanged()
        binding.tvEmptyPrivate.visibility = if (files.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun openPicker() {
        filePickerLauncher.launch(
            Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "*/*"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
        )
    }

    private fun handlePickedFile(uri: Uri) {
        val name = uri.lastPathSegment?.substringAfterLast("/")
            ?: "private_${System.currentTimeMillis()}"
        if (FileManager.savePrivateFile(this, uri, name)) {
            Toast.makeText(this, "File encrypted & saved!", Toast.LENGTH_SHORT).show()
            loadFiles()
        } else {
            Toast.makeText(this, "Encryption failed", Toast.LENGTH_SHORT).show()
        }
    }

    class PrivateAdapter(
        private val items: List<File>,
        private val onDelete: (File) -> Unit
    ) : RecyclerView.Adapter<PrivateAdapter.VH>() {

        inner class VH(view: View) : RecyclerView.ViewHolder(view) {
            val tvName: TextView = view.findViewById(R.id.tvFileName)
            val tvSize: TextView = view.findViewById(R.id.tvFileSize)
            val tvDel:  TextView = view.findViewById(R.id.tvDeleteFile)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            VH(LayoutInflater.from(parent.context).inflate(R.layout.item_file, parent, false))

        override fun onBindViewHolder(holder: VH, position: Int) {
            val f = items[position]
            holder.tvName.text = "🔒 ${FileManager.displayName(f)}"
            holder.tvSize.text = FileManager.readableSize(f.length())
            holder.tvDel.setOnClickListener { onDelete(f) }
        }

        override fun getItemCount() = items.size
    }
}
