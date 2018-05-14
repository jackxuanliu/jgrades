package website.jackl.jgrades.fragment

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.TaskStackBuilder
import android.support.v7.preference.CheckBoxPreference
import android.support.v7.preference.ListPreference
import android.support.v7.preference.PreferenceFragmentCompat
import android.support.v7.preference.PreferenceManager
import website.jackl.jgrades.R
import website.jackl.jgrades.activity.DEFAULT_CLASS_UPDATES_INTERVAL_MINUTES
import website.jackl.jgrades.activity.GradesActivity
import website.jackl.jgrades.activity.SettingsActivity


class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {
    interface Binder {
        val activity: GradesActivity<*>
        fun onClassNotificationToggle(status: Boolean)
        fun onClassNotificationInterval(minutes: Long)

        fun onPurchaseClick()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        binder = context as Binder
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)

        val interval = preferenceScreen.sharedPreferences.classUpdateInterval
        updateListSummaries()

        findPreference("pref_purchasePremium").setOnPreferenceClickListener {
            binder.onPurchaseClick()

            false
        }

        findPreference("pref_deleteLocalCachedData").setOnPreferenceClickListener {
            binder.activity.store.deleteGradebookData()
            false
        }

        findPreference("pref_gpFeedback").setOnPreferenceClickListener { launchMarket(); false }

//        findPreference("pref_exportGradebooks").setOnPreferenceClickListener {
//            if (ContextCompat.checkSelfPermission(binder.activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(binder.activity, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
//            } else {
//                val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
//                val exportFile = File(downloads, "Gradebooks Export.json")
//                exportFile.writeText(binder.activity.store.exportGradebooks())
//            }
//            false
//        }

    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)

    }

    override fun onSharedPreferenceChanged(prefs: SharedPreferences?, key: String?) {
        if (prefs != null && key != null) {
            when (key) {
                "pref_enableNotifications" -> {
                    if (Build.VERSION.SDK_INT < 21) {
                        binder.activity.showSnackbar(R.string.snackbar_updateForNotifications, Snackbar.LENGTH_LONG)
                        (findPreference(key) as CheckBoxPreference).isChecked = false
                        return
                    }
                    binder.onClassNotificationToggle(prefs.getBoolean(key, false))
                }
                "pref_updateInterval" -> {
                    val interval = prefs.classUpdateInterval

                    updateListSummaries()
                    binder.onClassNotificationInterval(interval)
                }
                "pref_theme" -> {
                    TaskStackBuilder.create(binder.activity)
                            .addNextIntentWithParentStack(
                                    Intent(binder.activity, SettingsActivity::class.java)
                            )
                            .startActivities()
                    binder.activity.finish()
                }
            }
        }
    }

    private fun updateListSummaries() {
        (findPreference("pref_updateInterval") as ListPreference).apply {
            summary = entry
        }
        (findPreference("pref_theme") as ListPreference).apply {
            summary = entry
        }
    }

    private fun launchMarket() {
        val uri = Uri.parse("market://details?id=" + binder.activity.packageName)
        val myAppLinkToMarket = Intent(Intent.ACTION_VIEW, uri)
        try {
            startActivity(myAppLinkToMarket)
        } catch (e: ActivityNotFoundException) {
            binder.activity.showSnackbar(R.string.error_googlePlay, Snackbar.LENGTH_SHORT)
        }

    }

    private lateinit var binder: Binder

}

fun SharedPreferences.getLongFromString(key: String, default: Long): Long {
    var value: Long
    try {
        value = getString(key, null).toLong()
    } catch (e: Throwable) {
        value = default
    }

    return value
}

val SharedPreferences.classUpdateInterval: Long
    get() {
        return getLongFromString("pref_updateInterval", DEFAULT_CLASS_UPDATES_INTERVAL_MINUTES)
    }

val SharedPreferences.classUpdateStatus: Boolean
    get() {
        return getBoolean("pref_enableNotifications", false)
    }

val Context.defaultPrefs get() = PreferenceManager.getDefaultSharedPreferences(this)