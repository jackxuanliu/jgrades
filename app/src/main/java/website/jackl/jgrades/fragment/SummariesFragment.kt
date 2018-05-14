package website.jackl.jgrades.fragment

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import website.jackl.jgrades.Data.Gradebook
import website.jackl.jgrades.Data.SettingsManager
import website.jackl.jgrades.Data.Student
import website.jackl.jgrades.Data.User
import website.jackl.jgrades.R
import website.jackl.jgrades.activity.GradesActivity
import website.jackl.jgrades.protocol.service.ServerService
import website.jackl.jgrades.recyclerAdapter.SummariesAdapter
import website.jackl.jgrades.view.MyList

/**
 * Created by jack on 2/3/18.
 */
class SummariesFragment : Fragment() {

    interface Binder {
        fun onStudentName(name: String?)

        val activity: GradesActivity<*>
        val store: SettingsManager
        val service: ServerService.Connection.Binder
        val connection: ServerService.Connection

        data class OnSummaryClickedResult(val summary: Gradebook.Summary, val student: Student.Info)

        fun onSummaryClicked(result: OnSummaryClickedResult)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val list = inflater.inflate(R.layout.fragment_summaries, container, false) as MyList

        val adapter = SummariesAdapter()
        this.adapter = adapter

        adapter.onItemClick = fun(position: Int, gradebook: Gradebook) { binder.onSummaryClicked(Binder.OnSummaryClickedResult(gradebook.summary, savedStudent!!)) }
        list.setAdapter(adapter)
        list.onSrlPull = this::fetchSummaries

        this.list = list
        return list
    }

    override fun onResume() {
        super.onResume()
        val locks = binder.store.loadNotificationLocks()
        binder.store.saveNotificationLocks(locks.copy(summariesLock = true))
    }

    override fun onPause() {
        super.onPause()
        val locks = binder.store.loadNotificationLocks()
        binder.store.saveNotificationLocks(locks.copy(summariesLock = false))
    }


    override fun onAttach(context: Context?) {
        super.onAttach(context)

        binder = context as Binder
    }

    fun restart() {
        val binder = binder

        user = binder.store.loadUser(binder.store.loadGlobal().activeEmail!!)
        preferredStudent = user!!.preferredStudent

        val list = list!!

        displaySavedSummaries()

        list.startLoading()
        fetchSummaries()

    }

    private fun displaySavedSummaries() {
        val binder = binder
        val list = list!!
        val preferredStudent = preferredStudent

        adapter.boldUpdated = binder.activity.defaultPrefs.getBoolean("pref_boldUpdated", true)

        if (preferredStudent != null) {
            adapter.items.clear()
            val summaries = binder.store.loadGradebookSummaries(preferredStudent)

            for (summary in summaries) {
                binder.store.loadGradebook(preferredStudent, summary.numberTerm)?.apply {
                    adapter.items.add(this)
                }
            }

            binder.onStudentName(preferredStudent.nameOfStudent)
            savedStudent = preferredStudent

            val newSummaries = mutableListOf<Gradebook.Summary>()
            binder.store.saveGradebookSummaries(preferredStudent, newSummaries)
        } else {
            adapter.items.clear()
            val student = binder.store.loadAnyStudent()?.info

            if (student != null) {
                val summaries = binder.store.loadGradebookSummaries(student)
                for (summary in summaries) {
                    binder.store.loadGradebook(student, summary.numberTerm)?.apply {
                        adapter.items.add(this)
                    }
                }

                this.preferredStudent = student
                user = binder.store.loadUser(binder.store.loadGlobal().activeEmail!!)
                binder.store.saveUser(user!!.copy(preferredStudent = student))
            }

            binder.onStudentName(student?.nameOfStudent)
            savedStudent = student
        }
        adapter.notifyDataSetChanged()
        list.stopLoading()
    }

    private fun fetchSummaries() {
        val desire = ServerService.Desire.Summary({
            displaySavedSummaries()
        }, { binder.activity.onErrorSnackbar(it); list!!.stopLoading() })
        binder.service.addDesire(desire)
    }

    private lateinit var store: SettingsManager
    private var list: MyList? = null
    private lateinit var adapter: SummariesAdapter
    private lateinit var binder: Binder

    private var user: User? = null
    private var preferredStudent: Student.Info? = null

    private var savedStudent: Student.Info? = null
}