package com.securedocs.app.ads

import android.app.Activity
import android.util.Log
import android.view.View
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.securedocs.app.utils.Prefs

/**
 * AdMob Ad Manager — crash-safe, premium-aware.
 *
 * All ad unit IDs are Google's official TEST IDs.
 * Replace with real IDs before publishing to Play Store.
 *
 * ✅ Banner load        — loadBanner()
 * ✅ Interstitial load  — loadInterstitial()
 * ✅ Interstitial show  — showInterstitial() with try/catch crash guard
 * ✅ Premium check      — ads auto-hidden for premium users
 */
object AdManager {

    const val BANNER_TEST_ID       = "ca-app-pub-3940256099942544/6300978111"
    const val INTERSTITIAL_TEST_ID = "ca-app-pub-3940256099942544/1033173712"

    private const val TAG = "AdManager"

    @Volatile private var interstitialAd: InterstitialAd? = null
    private var isLoadingInterstitial = false

    // ── Banner ────────────────────────────────────────────────────────────────

    fun loadBanner(adView: AdView) {
        if (Prefs.isPremium()) {
            adView.visibility = View.GONE
            return
        }
        try {
            adView.visibility = View.VISIBLE
            adView.loadAd(AdRequest.Builder().build())
        } catch (e: Exception) {
            Log.e(TAG, "Banner load failed: ${e.message}")
            adView.visibility = View.GONE
        }
    }

    // ── Interstitial Load ─────────────────────────────────────────────────────

    /**
     * Pre-loads interstitial so it is instantly available to show.
     * Safe to call multiple times — skips if already loading or loaded.
     */
    fun loadInterstitial(activity: Activity) {
        if (Prefs.isPremium()) return
        if (interstitialAd != null) return     // already loaded
        if (isLoadingInterstitial) return      // already loading

        isLoadingInterstitial = true
        try {
            InterstitialAd.load(
                activity,
                INTERSTITIAL_TEST_ID,
                AdRequest.Builder().build(),
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        interstitialAd = ad
                        isLoadingInterstitial = false
                        Log.d(TAG, "Interstitial loaded ✅")
                    }
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        interstitialAd = null
                        isLoadingInterstitial = false
                        Log.w(TAG, "Interstitial load failed: ${error.message}")
                    }
                }
            )
        } catch (e: Exception) {
            isLoadingInterstitial = false
            Log.e(TAG, "Interstitial load exception: ${e.message}")
        }
    }

    // ── Interstitial Show ─────────────────────────────────────────────────────

    /**
     * Shows interstitial if ready.
     * If not ready (premium / not loaded / error) → [onDismiss] is called directly.
     * Fully crash-safe with try/catch.
     */
    fun showInterstitial(activity: Activity, onDismiss: () -> Unit) {
        if (Prefs.isPremium()) {
            onDismiss()
            return
        }

        val ad = interstitialAd
        if (ad == null) {
            // No ad ready → proceed without interruption
            loadInterstitial(activity)   // start loading for next time
            onDismiss()
            return
        }

        try {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    loadInterstitial(activity)   // pre-load next ad
                    onDismiss()
                }
                override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                    interstitialAd = null
                    Log.w(TAG, "Interstitial show failed: ${error.message}")
                    onDismiss()
                }
                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Interstitial shown ✅")
                }
            }
            ad.show(activity)
        } catch (e: Exception) {
            Log.e(TAG, "Interstitial show exception: ${e.message}")
            interstitialAd = null
            onDismiss()   // never block the user flow
        }
    }
}
