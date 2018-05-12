package website.jackl.jgrades.fragment

import android.support.v4.app.Fragment
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.NotificationManagerCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.*
import org.json.JSONObject
import website.jackl.generated.data.constructGradebookAssignment
import website.jackl.generated.data.write

import website.jackl.jgrades.R
import website.jackl.jgrades.activity.GradesActivity
import website.jackl.jgrades.Data.Gradebook
import website.jackl.jgrades.Data.SettingsManager
import website.jackl.jgrades.Data.Student
import website.jackl.jgrades.activity.AssignmentEditActivity
import website.jackl.jgrades.protocol.service.ServerService
import website.jackl.jgrades.recyclerAdapter.AssignmentsAdapter
import website.jackl.jgrades.view.MyList

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [AssignmentsFragment.Binder] interface
 * to handle interaction events.
 */
class AssignmentsFragment : Fragment() {

    interface Binder {
        val activity: GradesActivity<*>
        val service: ServerService.Connection.Binder
        val connection: ServerService.Connection
        val store: SettingsManager

        val currentStudent: Student.Info
        val currentNumberTerm: String

        fun onRealScore(score: Double?)
        fun onEditScore(score: Double?)

        fun onAssignmentClicked(assignment: Gradebook.Assignment)

        fun displayEditMenu()
        fun displayViewMenu()

        fun showFab(animate: Boolean = true)
        fun hideFab(animate: Boolean = true)
    }

    fun forceUpdate() {
        binder.service.removeDesire()
        list.stopLoading()

        binder.apply {
            val gradebook = store.loadGradebook(currentStudent, currentNumberTerm)
            if (gradebook != null) {
                store.saveGradebook(currentStudent, gradebook.copy(lastDetailsCheck = 0L))
            }
        }

        fetchGradebook()
    }

    fun onFabClick() {
        binder.service.removeDesire()
        list.stopLoading()
        launchEdit()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val parent = inflater.inflate(R.layout.fragment_assignments, container, false)
        list =  parent.findViewById(R.id.assignments_list)

        adapter = AssignmentsAdapter()
        adapter.apply {
            onItemClick = fun(position: Int, assignment: Gradebook.Assignment) {
                binder.onAssignmentClicked(assignment)
                val oldPosition = sliderPosition
                sliderPosition = position
                notifyItemChanged(position)
                oldPosition?.apply { notifyItemChanged(this) }
            }
            onItemLongClick = fun(position: Int, assignment: Gradebook.Assignment) {
                binder.service.removeDesire()
                list.stopLoading()
                launchEdit(position, assignment)
            }
            onSlid = this@AssignmentsFragment::onSlid
        }
        list.setAdapter(adapter)
        list.onSrlPull = {
            fetchGradebook()
        }

        list.backingList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                if (dy > 0) {
                    binder.hideFab()
                } else if (dy < 0) {
                    binder.showFab()
                }
            }
        })

        return parent
    }

    override fun onResume() {
        super.onResume()
        val locks = binder.store.loadNotificationLocks()

        val newGradebookLocks = mutableListOf<String>()
        var exists = false
        for (lock in locks.gradebookLocks) {
            if (lock == binder.currentNumberTerm) exists = true
            newGradebookLocks.add(lock)
        }

        if (!exists) newGradebookLocks.add(binder.currentNumberTerm)

        binder.store.saveNotificationLocks(locks.copy(gradebookLocks = newGradebookLocks))

        val manager = NotificationManagerCompat.from(binder.activity)
        manager.cancel(binder.currentNumberTerm.hashCode())
    }

    override fun onPause() {
        super.onPause()
        val locks = binder.store.loadNotificationLocks()

        val newGradebookLocks = mutableListOf<String>()
        for (lock in locks.gradebookLocks) {
            if (lock != binder.currentNumberTerm) newGradebookLocks.add(lock)

        }

        binder.store.saveNotificationLocks(locks.copy(gradebookLocks = newGradebookLocks))
    }

    fun restart() {
        displaySavedGradebook()

        list.startLoading()
        fetchGradebook()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == AssignmentEditActivity.ASSIGNMENT_EDIT_REQUEST && resultCode == AppCompatActivity.RESULT_OK && data != null) {
            when (data.action) {
                Intent.ACTION_INSERT -> {
                    adapter.items.add(0, constructGradebookAssignment(JSONObject(data.getStringExtra("assignment")))!!)
                }
                Intent.ACTION_EDIT -> {
                    adapter.items.set(
                            data.getIntExtra("position", -1),
                            constructGradebookAssignment(JSONObject(data.getStringExtra("assignment")))!!)

                }
                Intent.ACTION_DELETE -> {
                    adapter.items.removeAt(data.getIntExtra("position", -1))
                }
            }
            onSlid()
            adapter.notifyDataSetChanged()
        }
    }

    private fun displaySavedGradebook() {

        if (!inEdit) {
            val adapter = list.getAdapter<Gradebook.Assignment>()
            val gradebook = binder.store.loadGradebook(binder.currentStudent, binder.currentNumberTerm)
            val details = gradebook?.details

            if (details != null) {
                if (details.assignments != adapter.items) {
                    adapter.items.clear()
                    adapter.items.addAll(details.assignments)
                }

                adapter.notifyDataSetChanged()

                binder.store.saveGradebook(binder.currentStudent, gradebook.copy(details = details.copy(), lastView = gradebook.summary.lastUpdated))
            } else {
                adapter.items.clear()
                adapter.notifyDataSetChanged()
            }

            savedDetails = details

            list.stopLoading()

            val savedDetails = savedDetails

            if (savedDetails != null) {
                binder.onRealScore(savedDetails.calculateGrade())
                binder.showFab(false)
            } else {
                binder.onRealScore(null)
                binder.hideFab(false)
            }
        }
    }

    private fun fetchGradebook() {
        binder.service.addDesire(ServerService.Desire.SingleGradebook(binder.currentNumberTerm, {
            displaySavedGradebook()
        }, {
            binder.activity.onErrorSnackbar(it)
            list.stopLoading()
        }))
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        binder = context as Binder

    }

    private fun onSlid() {
        enterEdit()
        calculateEditGrade()
    }

    private fun calculateEditGrade() {
        savedDetails?.apply {
            binder.onEditScore(calculateGrade(adapter.items))
        }
    }

    fun enterEdit() {
        if (!inEdit) {
            list.onSrlPull = {
                list.stopLoading()
                Handler().postDelayed({exitEdit()}, 300)

            }
            inEdit = true
            binder.service.removeDesire()
            list.stopLoading()
            binder.displayEditMenu()
        }
    }

    fun exitEdit() {
        if (inEdit) {
            list.onSrlPull = {
                fetchGradebook()
            }
            inEdit = false

            binder.displayViewMenu()
            binder.onEditScore(null)

            displaySavedGradebook()
            adapter.sliderPosition = null
        }
    }

    var inEdit = false

    private fun launchEdit(position: Int, assignment: Gradebook.Assignment) {
        val intent = Intent(binder.activity, AssignmentEditActivity::class.java)
        intent.putExtra("position", position)
        intent.putExtra("assignment", assignment.write().toString())
        val categoriesArrayList = arrayListOf<String>()
        for (category in savedDetails!!.detailedSummaryData.categories.values) {
            categoriesArrayList.add(category.name)
        }
        intent.putExtra("categories", categoriesArrayList)
        startActivityForResult(intent, AssignmentEditActivity.ASSIGNMENT_EDIT_REQUEST)
    }

    private fun launchEdit() {
        val intent = Intent(binder.activity, AssignmentEditActivity::class.java)
        val categoriesArrayList = arrayListOf<String>()
        for (category in savedDetails!!.detailedSummaryData.categories.values) {
            categoriesArrayList.add(category.name)
        }
        intent.putExtra("categories", categoriesArrayList)
        startActivityForResult(intent, AssignmentEditActivity.ASSIGNMENT_EDIT_REQUEST)
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html) for more information.
     */
    lateinit private var binder: Binder
    lateinit private var list: MyList
    lateinit private var adapter: AssignmentsAdapter


    private var savedDetails: Gradebook.Details? = null

}// Required empty public constructor
