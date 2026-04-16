package com.securedocs.app.billing

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.android.billingclient.api.*
import com.securedocs.app.utils.Prefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Google Play Billing — complete implementation.
 *
 * ✅ Purchase flow       — launchPurchase()
 * ✅ Acknowledge check   — prevents refund loop
 * ✅ Restore purchases   — queryExistingPurchases() on connect
 * ✅ Edge case safety    — all states handled
 *
 * Product ID: "secure_docs_premium"  (create this in Play Console)
 */
class BillingManager(
    private val activity: Activity,
    private val onPremiumGranted: () -> Unit
) {

    companion object {
        private const val TAG       = "BillingManager"
        const val PRODUCT_ID        = "secure_docs_premium"
    }

    private lateinit var billingClient: BillingClient
    private val scope = CoroutineScope(Dispatchers.Main)

    // ── Purchase listener (called on new purchases AND updates) ───────────────
    private val purchasesUpdatedListener = PurchasesUpdatedListener { result, purchases ->
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { handlePurchase(it) }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Toast.makeText(activity, "Purchase cancel हो गया।", Toast.LENGTH_SHORT).show()
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                // User already owns this — restore it
                queryExistingPurchases()
            }
            else -> {
                Log.w(TAG, "Purchase error: ${result.responseCode} — ${result.debugMessage}")
                Toast.makeText(activity, "Error: ${result.debugMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ── Init ──────────────────────────────────────────────────────────────────

    fun init() {
        billingClient = BillingClient.newBuilder(activity)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Billing connected — restoring purchases")
                    queryExistingPurchases()   // ✅ Restore on every connect
                } else {
                    Log.w(TAG, "Billing setup failed: ${result.debugMessage}")
                }
            }
            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "Billing disconnected")
            }
        })
    }

    // ── Restore existing purchases ────────────────────────────────────────────

    private fun queryExistingPurchases() {
        scope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    billingClient.queryPurchasesAsync(
                        QueryPurchasesParams.newBuilder()
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build()
                    )
                }
                if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    result.purchasesList.forEach { handlePurchase(it) }
                    Log.d(TAG, "Restore check: ${result.purchasesList.size} purchase(s) found")
                }
            } catch (e: Exception) {
                Log.e(TAG, "queryExistingPurchases failed: ${e.message}")
            }
        }
    }

    // ── Handle purchase (new + restored) ──────────────────────────────────────

    private fun handlePurchase(purchase: Purchase) {
        if (!purchase.products.contains(PRODUCT_ID)) return

        when (purchase.purchaseState) {
            Purchase.PurchaseState.PURCHASED -> {
                // ✅ Acknowledge first (mandatory — else Google refunds after 3 days)
                if (!purchase.isAcknowledged) {
                    acknowledgePurchase(purchase)
                } else {
                    // Already acknowledged → grant immediately
                    grantPremium()
                }
            }
            Purchase.PurchaseState.PENDING -> {
                // Payment initiated but not yet completed (UPI/bank pending)
                Log.d(TAG, "Purchase pending — waiting for completion")
                Toast.makeText(
                    activity,
                    "Payment pending है। Complete होने पर Premium unlock होगा।",
                    Toast.LENGTH_LONG
                ).show()
            }
            else -> Log.d(TAG, "Purchase state unknown: ${purchase.purchaseState}")
        }
    }

    // ── Acknowledge ───────────────────────────────────────────────────────────

    private fun acknowledgePurchase(purchase: Purchase) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient.acknowledgePurchase(params) { result ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d(TAG, "Purchase acknowledged ✅")
                grantPremium()
            } else {
                Log.e(TAG, "Acknowledge failed: ${result.debugMessage}")
                // Still grant locally — acknowledge will retry next session
                grantPremium()
            }
        }
    }

    // ── Grant premium ─────────────────────────────────────────────────────────

    private fun grantPremium() {
        Prefs.setPremium(true)
        activity.runOnUiThread { onPremiumGranted() }
    }

    // ── Launch purchase flow ───────────────────────────────────────────────────

    fun launchPurchase() {
        if (!::billingClient.isInitialized || !billingClient.isReady) {
            Toast.makeText(activity, "Billing connect हो रहा है। थोड़ा wait करें।", Toast.LENGTH_SHORT).show()
            init()
            return
        }

        scope.launch {
            try {
                val params = QueryProductDetailsParams.newBuilder()
                    .setProductList(
                        listOf(
                            QueryProductDetailsParams.Product.newBuilder()
                                .setProductId(PRODUCT_ID)
                                .setProductType(BillingClient.ProductType.INAPP)
                                .build()
                        )
                    )
                    .build()

                val result = withContext(Dispatchers.IO) {
                    billingClient.queryProductDetails(params)
                }

                val product = result.productDetailsList?.firstOrNull()
                if (product == null) {
                    Toast.makeText(
                        activity,
                        "Product नहीं मिला। Internet check करें।",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

                val flowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(
                        listOf(
                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(product)
                                .build()
                        )
                    )
                    .build()

                billingClient.launchBillingFlow(activity, flowParams)

            } catch (e: Exception) {
                Log.e(TAG, "launchPurchase failed: ${e.message}")
                Toast.makeText(activity, "Purchase error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ── Cleanup ───────────────────────────────────────────────────────────────

    fun destroy() {
        if (::billingClient.isInitialized && billingClient.isReady) {
            billingClient.endConnection()
        }
    }
}
