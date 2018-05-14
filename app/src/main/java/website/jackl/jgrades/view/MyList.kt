package website.jackl.jgrades.view

import android.content.Context
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import website.jackl.jgrades.R
import website.jackl.jgrades.recyclerAdapter.MyListAdapter

/**
 * Created by jack on 2/2/18.
 */
class MyList(context: Context, attributeSet: AttributeSet) : FrameLayout(context, attributeSet) {


    fun <T> getAdapter(): MyListAdapter<T> {
        return backingList.adapter as MyListAdapter<T>
    }

    fun <T> setAdapter(adapter: MyListAdapter<T>) {
        backingList.adapter = adapter
    }

    var onSrlPull: (() -> Unit)? = null

    fun disableSrl() {
        srl.isEnabled = false
    }

    fun enableSrl() {
        srl.isEnabled = true
    }

    fun startLoading() {
        val adapter = getAdapter<Any?>()

        if (adapter == null) {
            throw RuntimeException("adapter not set")
        }

        Log.d("items", adapter.items.isEmpty().toString())
        Log.d("srl", srl.isRefreshing.toString())

        if (adapter.items.isEmpty()) {
            if (!srl.isRefreshing) {
                srl.visibility = View.GONE
                progressBar.visibility = View.VISIBLE
                empty.visibility = View.GONE
            } else { // if srl is already loading, show that instead
                progressBar.visibility = View.GONE
                srl.visibility = View.VISIBLE
                backingList.visibility = View.GONE
            }
        } else { // if not empty
            srl.visibility = View.VISIBLE
            backingList.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
            empty.visibility = View.GONE
        }

    }

    fun stopLoading() {
        val adapter = getAdapter<Any?>()

        if (adapter == null) {
            throw RuntimeException("adapter not set")
        }

        srl.visibility = View.VISIBLE
        srl.isRefreshing = false
        progressBar.visibility = View.GONE

        if (adapter.items.isEmpty()) {
            backingList.visibility = View.GONE
            empty.visibility = View.VISIBLE
            emptyTitle.setText(adapter.emptyTitleId)
        } else {
            backingList.visibility = View.VISIBLE
            empty.visibility = View.GONE
        }
    }

    private val progressBar: ProgressBar
    private val srl: SwipeRefreshLayout
    private val frame: FrameLayout
    private val empty: View
    private val emptyTitle: TextView
    val backingList: RecyclerView

    init {
        val inflater = LayoutInflater.from(context)
        frame = inflater.inflate(R.layout.view_mylist, this) as FrameLayout

        backingList = frame.findViewById<RecyclerView>(R.id.mylist_list)

        val layoutManager = LinearLayoutManager(context)
        backingList.addItemDecoration(DividerItemDecoration(context, layoutManager.orientation))

        progressBar = frame.findViewById(R.id.mylist_progressBar)
        srl = frame.findViewById(R.id.mylist_srl)
        srl.setOnRefreshListener { onSrlPull?.invoke() }

        empty = findViewById(R.id.mylist_empty)
        emptyTitle = findViewById(R.id.empty_title)
    }
}