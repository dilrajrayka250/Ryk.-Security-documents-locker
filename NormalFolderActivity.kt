package com.securedocs.app.ui

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.securedocs.app.R
import com.securedocs.app.ads.AdManager
import com.securedocs.app.databinding.ActivityNormalBinding
import com.securedocs.app.storage.FileManager
import com.securedocs.app.utils.PermissionHelper
import java.io.File

class NormalFolderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNormalBinding
    private val files = mutableListOf<File>()

    // ── Activity Result API (modern, non-deprecated) ──────────────────────────
    private val filePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri -> handlePickedFile(uri) }
            }
        }

    private val storagePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) openFilePicker()
            else Toast.makeText(this, "Storage permission required", Toast.LENGTH_SHORT).show()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNormalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AdManager.loadBanner(binding.adViewNormal)
        setupRecyclerView()
        loadFiles()

        binding.fabAddNormal.setOnClickListener { checkPermissionAndPick() }
    }

    private fun setupRecyclerView() {
        binding.rvNormalFiles.layoutManager = LinearLayoutManager(this)
        binding.rvNormalFiles.adapter = FileAdapter(files) { file ->
            FileManager.delete(file)
            Toast.makeText(this, "Deleted: ${file.name}", Toast.LENGTH_SHORT).show()
            loadFiles()
        }
    }

    private fun loadFiles() {
        files.clear()
        files.addAll(FileManager.listNormalFiles(this))
        binding.rvNormalFiles.adapter?.notifyDataSetChanged()
        binding.tvEmptyNormal.visibility = if (files.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun checkPermissionAndPick() {
        if (PermissionHelper.hasStoragePermission(this)) {
            openFilePicker()
        } else {
            val permission = PermissionHelper.getStoragePermission()
            storagePermissionLauncher.launch(permission)
        }
    }

    private fun openFilePicker() {
        filePickerLauncher.launch(
            Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "*/*"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
        )
    }

    private fun handlePickedFile(uri: Uri) {
        val name = uri.lastPathSegment?.substringAfterLast("/")
            ?: "doc_${System.currentTimeMillis()}"
        if (FileManager.saveNormalFile(this, uri, name)) {
            Toast.makeText(this, "File saved!", Toast.LENGTH_SHORT).show()
            loadFiles()
        } else {
            Toast.makeText(this, "Could not save file.", Toast.LENGTH_SHORT).show()
        }
    }

    // ── RecyclerView Adapter ──────────────────────────────────────────────────

    class FileAdapter(
        private val items: List<File>,
        private val onDelete: (File) -> Unit
    ) : RecyclerView.Adapter<FileAdapter.VH>() {

        inner class VH(view: View) : RecyclerView.ViewHolder(view) {
            val tvName: TextView = view.findViewById(R.id.tvFileName)
            val tvSize: TextView = view.findViewById(R.id.tvFileSize)
            val tvDel:  TextView = view.findViewById(R.id.tvDeleteFile)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            VH(LayoutInflater.from(parent.context).inflate(R.layout.item_file, parent, false))

        override fun onBindViewHolder(holder: VH, position: Int) {
            val f = items[position]
            holder.tvName.text = f.name
            holder.tvSize.text = FileManager.readableSize(f.length())
            holder.tvDel.setOnClickListener { onDelete(f) }
        }

        override fun getItemCount() = items.size
    }
}
