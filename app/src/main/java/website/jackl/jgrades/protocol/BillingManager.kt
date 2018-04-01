package website.jackl.jgrades.protocol

import android.content.Context
import android.os.Handler
import com.android.billingclient.api.*
import website.jackl.jgrades.activity.GradesActivity
import website.jackl.jgrades.fragment.defaultPrefs

/**
 * Created by jack on 2/24/18.
 */
class BillingManager(val binder: Binder): PurchasesUpdatedListener, BillingClientStateListener {
    interface Binder {
        val context: Context
        fun checkPurchases()
    }

    fun purchasePremium(activity: GradesActivity<*>) {
        val flowParams = BillingFlowParams.newBuilder()
                .setSku("lifetime_premium")
                .setType(BillingClient.SkuType.INAPP)
                .build()

        billingClient.launchBillingFlow(activity, flowParams)
    }

    fun onStart() {
        if (!started) {
            started = true
            billingClient = BillingClient.newBuilder(binder.context).setListener(this).build()

                billingClient.startConnection(this)

        } else {
            onStop()
            onStart()
        }
    }

    fun onStop() {
        if (started) {
            started = false
                billingClient.endConnection()

        }
    }

    override fun onBillingSetupFinished(responseCode: Int) {
        if (responseCode == BillingClient.BillingResponse.OK) {
            val result = billingClient.queryPurchases(BillingClient.SkuType.INAPP)
            if (result.responseCode == BillingClient.BillingResponse.OK) {
                var isPremium = false
                for (purchase in result.purchasesList) {
                    if (purchase.sku == "lifetime_premium") {
                        isPremium = true
                        break
                    }
                }
                binder.context.defaultPrefs.edit().putBoolean("qwe", isPremium).commit()
                binder.checkPurchases()
            }
        }
    }

    override fun onBillingServiceDisconnected() {
        fun connect(delayMillis: Long) {
            if (started)
            handler.postDelayed({
                if (started) billingClient.startConnection(this)
            }, delayMillis)
        }
        when (tries) {
            0 -> {
                connect(2000)
            }
            1 -> {
                connect(4000)
            }
            2 -> {
                connect(8000)
            }
            else -> {
                started = false
            }
        }
        ++tries
    }

    override fun onPurchasesUpdated(responseCode: Int, purchases: MutableList<Purchase>?) {

    }

    private val handler = Handler()
    private lateinit var billingClient: BillingClient
    private var started: Boolean = false
    private var tries: Int = 0
}