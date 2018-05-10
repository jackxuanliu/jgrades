package website.jackl.jgrades.activity

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.support.v7.widget.Toolbar
import android.widget.FrameLayout
import android.widget.RelativeLayout
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import website.jackl.generated.data.write
import website.jackl.jgrades.R
import website.jackl.jgrades.Data.SettingsManager
import website.jackl.jgrades.fragment.SummariesFragment
import website.jackl.jgrades.fragment.defaultPrefs
import website.jackl.jgrades.newStore
import website.jackl.jgrades.protocol.BillingManager
import com.google.firebase.analytics.FirebaseAnalytics
import java.util.*


val VERSION = 10016
val VERSION_DESC = "Hello, adding a marker for updated classes has been a much requested feature, so I have implemented it. This option can be disabled in settings menu if you wish.\n\nThank you for your patience!"


class MainActivity : GradesActivity<ConstraintLayout>(), SummariesFragment.Binder, BillingManager.Binder{


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)




        coordinator = findViewById(R.id.coordinator)
        parent = findViewById(R.id.parent)

        toolbar = findViewById(R.id.toolbar)

        fragment = supportFragmentManager.findFragmentById(R.id.main_summaries) as SummariesFragment

        subtitle = findViewById(R.id.main_subtitle)

        adFrame = findViewById(R.id.adFrame)

        billingManager = BillingManager(this)

        toolbar.inflateMenu(R.menu.main)
        toolbar.setOnMenuItemClickListener(this::onMenuItemClicked)
    }

    override fun onStart() {
        super.onStart()
        showUpdateChanges()

        if (defaultPrefs.getLong("reviewTimer", -1) == -1L) {
            defaultPrefs.edit().putLong("reviewTimer", Date().time).commit()
        }
    }

    override fun onStop() {
        super.onStop()
        billingManager.onStop()
    }

    override fun onServiceReady() {
        fragment.restart()
        checkPurchases()
        billingManager.onStart()

    }

    override fun onStudentName(name: String?) {
        val subtitle = subtitle!!
        if (name == null) {
            subtitle.visibility = View.GONE
        } else {
            subtitle.visibility = View.VISIBLE
        }
        subtitle.text = name
    }

    override fun onSummaryClicked(result: SummariesFragment.Binder.OnSummaryClickedResult) {
        val intent = Intent(this, AssignmentsActivity::class.java)
        intent.putExtra("student", result.student.write().toString())
        intent.putExtra("numberTerm", result.summary.numberTerm)
        startActivity(intent)
    }

    override val activity: GradesActivity<*>
        get() = this

    private fun onMenuItemClicked(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.main_logout -> {
                val builder = AlertDialog.Builder(this)
                builder.setPositiveButton(android.R.string.yes, fun (dialog: DialogInterface, which: Int) {
                    logout()
                })

                builder.setNegativeButton(android.R.string.cancel, fun (dialog: DialogInterface, which: Int) {

                })
                builder.setTitle(R.string.dialogTitle_logout)
                builder.setMessage(R.string.dialogMsg_logout)

                builder.create().show()
            }
            R.id.main_changeStudent -> {
                val intent = Intent(this, SelectStudentActivity::class.java)
                startActivity(intent)
            }
            R.id.main_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }
        }
        return true
    }

    private fun logout() {
        val global = store.loadGlobal()
        store.saveGlobal(global.copy(activeEmail = null))
        launchLogin()
        finish()
    }

    private fun showUpdateChanges() {
        val savedVersion = defaultPrefs.getInt("savedVersion", 0)
        if (VERSION > savedVersion) {
            val builder = AlertDialog.Builder(activity)
            builder.setTitle("Update notes")
            builder.setMessage(VERSION_DESC)
            builder.setPositiveButton(android.R.string.ok, fun (dialog: DialogInterface, which: Int) {})
            builder.create().show()
            defaultPrefs.edit().putInt("savedVersion", VERSION).commit()
        }

    }

    override fun checkPurchases() {
        val premium = defaultPrefs.getBoolean("qwe", false)

        if (premium) {
            adFrame.removeAllViews()
        } else {
            service.attachAd(adFrame)
        }
    }

    override val context: Context
        get() = this

    private var subtitle: TextView? = null
    private lateinit var fragment: SummariesFragment
    private lateinit var toolbar: Toolbar
    private lateinit var adFrame: RelativeLayout

    private lateinit var billingManager: BillingManager

}
