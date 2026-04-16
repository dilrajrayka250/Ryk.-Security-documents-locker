package com.securedocs.app.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.securedocs.app.databinding.ActivityScannerBinding
import com.securedocs.app.storage.FileManager
import com.securedocs.app.utils.PermissionHelper
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScannerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScannerBinding
    private lateinit var cameraExecutor: ExecutorService
    private var scanning = true

    // ── Activity Result API (modern, non-deprecated) ──────────────────────────
    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) startCamera()
            else {
                Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraExecutor = Executors.newSingleThreadExecutor()

        if (PermissionHelper.hasCameraPermission(this)) {
            startCamera()
        } else {
            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    // ── Camera ────────────────────────────────────────────────────────────────

    private fun startCamera() {
        val providerFuture = ProcessCameraProvider.getInstance(this)
        providerFuture.addListener({
            try {
                val provider = providerFuture.get()
                val preview = Preview.Builder().build()
                    .also { it.setSurfaceProvider(binding.previewView.surfaceProvider) }

                val analyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { ia ->
                        ia.setAnalyzer(cameraExecutor) { proxy ->
                            if (!scanning) { proxy.close(); return@setAnalyzer }
                            val media = proxy.image
                            if (media == null) { proxy.close(); return@setAnalyzer }
                            val img = InputImage.fromMediaImage(
                                media, proxy.imageInfo.rotationDegrees
                            )
                            BarcodeScanning.getClient().process(img)
                                .addOnSuccessListener { barcodes ->
                                    barcodes.firstOrNull()?.let { bc ->
                                        scanning = false
                                        onBarcodeDetected(bc)
                                    }
                                }
                                .addOnCompleteListener { proxy.close() }
                        }
                    }

                provider.unbindAll()
                provider.bindToLifecycle(
                    this, CameraSelector.DEFAULT_BACK_CAMERA, preview, analyzer
                )
            } catch (e: Exception) {
                Toast.makeText(this, "Camera error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    // ── Barcode result ────────────────────────────────────────────────────────

    private fun onBarcodeDetected(barcode: Barcode) {
        val value = barcode.rawValue ?: return
        FileManager.saveQrResult(this, value)
        runOnUiThread {
            binding.tvScanStatus.text = "Scanned & Saved ✓"
            showResultDialog(value, barcode.valueType)
        }
    }

    private fun showResultDialog(value: String, type: Int) {
        val isUrl = type == Barcode.TYPE_URL
        AlertDialog.Builder(this)
            .setTitle("Scan Result")
            .setMessage(value)
            .setPositiveButton("Copy") { _, _ ->
                val clip = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clip.setPrimaryClip(ClipData.newPlainText("scan", value))
                Toast.makeText(this, "Copied!", Toast.LENGTH_SHORT).show()
                scanning = true
            }
            .apply {
                if (isUrl) {
                    setNeutralButton("Open URL") { _, _ ->
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(value)))
                        scanning = true
                    }
                }
            }
            .setNegativeButton("Scan Again") { _, _ -> scanning = true }
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
