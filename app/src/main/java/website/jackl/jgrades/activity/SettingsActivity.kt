package website.jackl.jgrades.activity

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.os.Bundle
import android.support.v7.preference.Preference
import android.widget.ScrollView
import website.jackl.jgrades.R
import website.jackl.jgrades.fragment.SettingsFragment
import android.app.NotificationManager
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.support.design.widget.Snackbar
import android.support.v7.preference.PreferenceManager
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingFlowParams
import website.jackl.jgrades.fragment.getLongFromString
import website.jackl.jgrades.protocol.BillingManager
import website.jackl.jgrades.service.NotificationService

val CLASS_UPDATES_CHANNEL_ID = "classUpdatesChannel"
val CLASS_UPDATES_JOB_ID = 1954875
val DEFAULT_CLASS_UPDATES_INTERVAL_MINUTES: Long = 15


class SettingsActivity : GradesActivity<ScrollView>(), SettingsFragment.Binder, BillingManager.Binder {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        parent = findViewById(R.id.parent)
        billingManager = BillingManager(this)
    }

    override fun onStart() {
        super.onStart()
        billingManager.onStart()
    }

    override fun onStop() {
        super.onStop()
        billingManager.onStop()
    }

    override fun onClassNotificationToggle(status: Boolean) {


        if (status == true) {
            setupNotificationChannel()
            val prefs = PreferenceManager.getDefaultSharedPreferences(this)
            enableNotificationJob(prefs.getLongFromString("pref_updateInterval", DEFAULT_CLASS_UPDATES_INTERVAL_MINUTES))
        } else {
            disableNotificationJob()
        }
    }

    override fun onClassNotificationInterval(minutes: Long) {
        enableNotificationJob(minutes)
    }

    @TargetApi(26) private fun setupNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            val name = getString(R.string.channel_classUpdates)
            val description = getString(R.string.channelDesc_classUpdates)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CLASS_UPDATES_CHANNEL_ID, name, importance)
            channel.description = description
            // Register the channel with the system
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

    }

    @TargetApi(21) private fun enableNotificationJob(intervalMinutes: Long) {
        val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val builder = JobInfo.Builder(CLASS_UPDATES_JOB_ID, ComponentName(this, NotificationService::class.java))

        builder.setPeriodic(intervalMinutes * 1000 * 60)
        builder.setPersisted(true)
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)

        jobScheduler.schedule(builder.build())
    }

    @TargetApi(21)private fun disableNotificationJob() {
        val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        jobScheduler.cancel(CLASS_UPDATES_JOB_ID)
    }

    override fun onPurchaseClick() {
        billingManager.purchasePremium(this)
    }

    override val context: Context
        get() = this

    override val activity: GradesActivity<*>
        get() = this

    override fun checkPurchases() {
        // nothing
    }

    private lateinit var billingManager: BillingManager

}
