package website.jackl.jgrades.activity

import android.app.Fragment
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.constraint.ConstraintLayout
import android.support.design.widget.FloatingActionButton
import android.transition.TransitionManager
import android.util.Log
import android.view.View
import android.support.v7.widget.Toolbar
import android.widget.*
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import org.json.JSONObject
import website.jackl.generated.data.constructStudentInfo
import website.jackl.jgrades.R
import website.jackl.jgrades.Data.Gradebook
import website.jackl.jgrades.Data.SettingsManager
import website.jackl.jgrades.Data.Student
import website.jackl.jgrades.fragment.AssignmentsFragment
import website.jackl.jgrades.fragment.defaultPrefs
import website.jackl.jgrades.newStore
import website.jackl.jgrades.protocol.BillingManager

/**
 * Created by jack on 1/28/18.
 */

class AssignmentsActivity : GradesActivity<ConstraintLayout>(), AssignmentsFragment.Binder, BillingManager.Binder {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assignments)

        coordinator = findViewById(R.id.coordinator)
        parent = findViewById(R.id.parent)

        toolbar = findViewById(R.id.toolbar)
        toolbar.setOnMenuItemClickListener { fragment.exitEdit(); true }

        fragment = supportFragmentManager.findFragmentById(R.id.assignments_list) as AssignmentsFragment

        percents = findViewById(R.id.assignments_percents)
        officialPercent = findViewById(R.id.officialPercent)
        editPercent = findViewById(R.id.editPercent)
        arrow = findViewById(R.id.arrow)

        addFab = findViewById(R.id.assignments_add)
        addFab.setOnClickListener { fragment.onFabClick() }

        val subtitle: TextView = findViewById(R.id.assignments_subtitle)
        subtitle.text = store.loadGradebook(currentStudent!!, currentNumberTerm!!)!!.summary.name

        adFrame = findViewById(R.id.adFrame)
        billingManager = BillingManager(this)


    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
        billingManager.onStop()
    }

    override fun onServiceReady() {
        super.onServiceReady()
        fragment.restart()
        billingManager.onStart()

        checkPurchases()
    }

    override val activity: GradesActivity<*>
        get() = this

    override val currentStudent: Student.Info
        get() {
            return constructStudentInfo(JSONObject(intent.getStringExtra("student")))!!


        }

    override val currentNumberTerm: String
        get() {
            return intent.getStringExtra("numberTerm")!!
        }

    override fun onAssignmentClicked(assignment: Gradebook.Assignment) {
        // TODO
    }

    override fun onRealScore(percent: Double?) {
            if (percent != null) {
                percents.visibility = View.VISIBLE
                officialPercent.text = Gradebook.classPercentFormat.format(percent)
            } else {
                percents.visibility = View.GONE
            }

    }

    override fun onEditScore(score: Double?) {
        if (score == null) {
            hideEditPercent()
        } else {
            showEditPercent()
            editPercent.text = Gradebook.classPercentFormat.format(score)
        }
    }

    override fun showFab(animate: Boolean) {
        if (animate) addFab.show() else addFab.visibility = View.VISIBLE
    }

    override fun hideFab(animate: Boolean) {
        Log.d("hide", animate.toString())
        if (animate) addFab.hide() else addFab.visibility = View.GONE
    }

    override fun checkPurchases() {
        val premium = defaultPrefs.getBoolean("qwe", false)

        if (premium) {
            adFrame.removeAllViews()
        } else {
            service.attachAd(adFrame)
        }
    }

    var shown = false

    private fun showEditPercent() {
        if (!shown) {
            TransitionManager.beginDelayedTransition(percents)
            arrow.visibility = View.VISIBLE
            editPercent.visibility = View.VISIBLE
//            Handler().postDelayed({
//                TransitionManager.beginDelayedTransition(percents)
//                arrow.visibility = View.VISIBLE
//                editPercent.visibility = View.VISIBLE
//            }, 200)
            shown = true
        }
    }

    private fun hideEditPercent() {
        if (shown) {
            TransitionManager.beginDelayedTransition(percents)
            arrow.visibility = View.GONE
            editPercent.visibility = View.GONE
//            Handler().postDelayed({
//                TransitionManager.beginDelayedTransition(percents)
//                arrow.visibility = View.GONE
//                editPercent.visibility = View.GONE
//            }, 500)
            shown = false
        }
    }


    override fun onEnterEdit() {
        toolbar.inflateMenu(R.menu.edit_mode)
    }

    override fun onExitEdit() {
        toolbar.menu.clear()
    }

    override val context: Context
        get() = this

    private var subtitle: TextView? = null
    private lateinit var percents: LinearLayout

    private lateinit var arrow: ImageView

    private lateinit var editPercent: TextView
    private lateinit var officialPercent: TextView

    private lateinit var fragment: AssignmentsFragment
    private lateinit var toolbar: Toolbar

    private lateinit var addFab: FloatingActionButton
    private lateinit var adFrame: RelativeLayout

    private lateinit var billingManager: BillingManager

}