package com.example.mydocumentapp

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.FullScreenContentCallback

class BackupActivity : AppCompatActivity() {

    private lateinit var adView: AdView
    private lateinit var btnBackupNow: Button
    private lateinit var btnWatchAd: Button
    private var rewardedAd: RewardedAd? = null

    companion object {
        // Test Rewarded Ad Unit ID
        private const val REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_backup)

        adView = findViewById(R.id.adViewBackup)
        btnBackupNow = findViewById(R.id.btnBackupNow)
        btnWatchAd = findViewById(R.id.btnWatchAdForBackup)

        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        loadRewardedAd()

        btnBackupNow.setOnClickListener {
            performBackup()
        }

        btnWatchAd.setOnClickListener {
            showRewardedAd()
        }
    }

    private fun loadRewardedAd() {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            this,
            REWARDED_AD_UNIT_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    rewardedAd = null
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                }
            }
        )
    }

    private fun showRewardedAd() {
        if (rewardedAd != null) {
            rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    loadRewardedAd()
                }
            }
            rewardedAd?.show(this) { rewardItem ->
                // User को reward मिला - extra backup space दें
                Toast.makeText(this, "Reward मिला: ${rewardItem.amount} ${rewardItem.type}", Toast.LENGTH_SHORT).show()
                performBackup()
            }
        } else {
            Toast.makeText(this, "Ad अभी load नहीं हुई, थोड़ा wait करें", Toast.LENGTH_SHORT).show()
        }
    }

    private fun performBackup() {
        Toast.makeText(this, "Backup शुरू हो गया...", Toast.LENGTH_SHORT).show()
        // TODO: Actual backup logic (Google Drive / local storage)
    }
}
