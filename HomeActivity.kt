package com.securedocs.app.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.securedocs.app.R
import com.securedocs.app.ads.AdManager
import com.securedocs.app.billing.BillingManager
import com.securedocs.app.databinding.ActivityHomeBinding
import com.securedocs.app.utils.Prefs

/**
 * Home Dashboard.
 * - Banner Ad at the bottom (free users only).
 * - Navigation to all feature screens.
 * - Premium upgrade dialog via BillingManager.
 */
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var billingManager: BillingManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBilling()
        setupClickListeners()
        updatePremiumUi()

        // Pre-load interstitial for NormalFolderActivity
        AdManager.loadInterstitial(this)

        // Banner ad at bottom
        AdManager.loadBanner(binding.adViewHome)
    }

    // ── Billing ───────────────────────────────────────────────────────────────

    private fun setupBilling() {
        billingManager = BillingManager(this) {
            // Called when premium is granted
            runOnUiThread { updatePremiumUi() }
            Toast.makeText(this, "Premium unlock हो गया! 🎉", Toast.LENGTH_LONG).show()
        }
        billingManager.init()
    }

    // ── UI helpers ────────────────────────────────────────────────────────────

    private fun updatePremiumUi() {
        val premium = Prefs.isPremium()
        binding.tvPremiumBadge.visibility = if (premium) View.VISIBLE else View.GONE
        binding.btnUpgradePremium.visibility = if (premium) View.GONE else View.VISIBLE
        binding.adViewHome.visibility = if (premium) View.GONE else View.VISIBLE
    }

    // ── Click listeners ───────────────────────────────────────────────────────

    private fun setupClickListeners() {

        binding.btnNormalDocs.setOnClickListener {
            // Show interstitial before opening Normal Docs
            AdManager.showInterstitial(this) {
                startActivity(Intent(this, NormalFolderActivity::class.java))
            }
        }

        binding.btnPrivateDocs.setOnClickListener {
            if (Prefs.isPremium()) {
                startActivity(Intent(this, PrivateFolderActivity::class.java))
            } else {
                showUpgradeDialog()
            }
        }

        binding.btnQrScan.setOnClickListener {
            startActivity(Intent(this, ScannerActivity::class.java))
        }

        binding.btnBackup.setOnClickListener {
            if (Prefs.isPremium()) {
                startActivity(Intent(this, BackupActivity::class.java))
            } else {
                showUpgradeDialog()
            }
        }

        binding.btnUpgradePremium.setOnClickListener {
            showUpgradeDialog()
        }
    }

    private fun showUpgradeDialog() {
        AlertDialog.Builder(this)
            .setTitle("Premium Unlock")
            .setMessage(
                "Premium features:\n\n" +
                "🔐 Private Encrypted Folder\n" +
                "☁️  Backup Documents\n" +
                "🚫 No Ads\n\n" +
                "One-time purchase — no subscription!"
            )
            .setPositiveButton("Upgrade Now") { _, _ ->
                billingManager.launchPurchase()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        billingManager.destroy()
    }
}
