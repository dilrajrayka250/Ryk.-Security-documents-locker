package com.securedocs.app.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.securedocs.app.databinding.ActivityBackupBinding
import com.securedocs.app.storage.FileManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Premium-only backup screen.
 * Copies all Normal + Private files into a backup folder.
 */
class BackupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBackupBinding
    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBackupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnStartBackup.setOnClickListener { startBackup() }
    }

    private fun startBackup() {
        binding.progressBackup.visibility = View.VISIBLE
        binding.tvBackupStatus.text = "Backup चल रहा है..."
        binding.btnStartBackup.isEnabled = false

        scope.launch {
            val count = withContext(Dispatchers.IO) { FileManager.backupAll(this@BackupActivity) }
            binding.progressBackup.visibility = View.GONE
            binding.tvBackupStatus.text = "Backup complete! $count files copied."
            binding.btnStartBackup.isEnabled = true
            Toast.makeText(this@BackupActivity, "Backup successful ✓", Toast.LENGTH_SHORT).show()
        }
    }
}
