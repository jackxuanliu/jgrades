package website.jackl.jgrades.activity

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AlertDialog
import android.support.v7.widget.Toolbar
import android.transition.TransitionManager
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import org.json.JSONObject
import website.jackl.generated.data.constructStudentInfo
import website.jackl.jgrades.Data.Gradebook
import website.jackl.jgrades.Data.Student
import website.jackl.jgrades.R
import website.jackl.jgrades.fragment.AssignmentsFragment
import website.jackl.jgrades.fragment.defaultPrefs
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
        toolbar.setOnMenuItemClickListener(this::onMenuItemClick)
        displayViewMenu()

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

    private fun onMenuItemClick(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.menuItem_forceUpdate -> {
                val builder = AlertDialog.Builder(this)
                builder.setPositiveButton(android.R.string.ok, fun(dialog: DialogInterface, which: Int) {
                    fragment.forceUpdate()
                })

                builder.setNegativeButton(android.R.string.cancel, fun(dialog: DialogInterface, which: Int) {

                })
                builder.setTitle(R.string.dialogTitle_forceUpdate)
                builder.setMessage(R.string.dialogMsg_forceUpdate)

                builder.create().show()
            }
            R.id.editMode_revert -> {
                fragment.exitEdit()
            }
        }
        return true
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


    override fun displayEditMenu() {
        toolbar.menu.clear()
        toolbar.inflateMenu(R.menu.edit_mode)
    }

    override fun displayViewMenu() {
        toolbar.menu.clear()
        toolbar.inflateMenu(R.menu.view_assignment)
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