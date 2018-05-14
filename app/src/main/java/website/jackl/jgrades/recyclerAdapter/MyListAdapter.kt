package website.jackl.jgrades.recyclerAdapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup


/**
 * [RecyclerView.Adapter] that can display a [DummyItem] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 * TODO: Replace the implementation with code for your data type.
 */
abstract class MyListAdapter<T> : RecyclerView.Adapter<MyListAdapter.ViewHolder<T>>() {
    var items: MutableList<T> = mutableListOf()

    var onItemClick: ((Int, T) -> Unit)? = null
    var onItemLongClick: ((Int, T) -> Unit)? = null

    abstract val itemLayoutId: Int
    abstract val emptyTitleId: Int

    abstract fun constructViewHolder(view: View): ViewHolder<T>

    override fun onBindViewHolder(holder: ViewHolder<T>, position: Int) {
        val item = items[position]
        holder.item = item
        holder.view.setOnClickListener {
            onItemClick?.invoke(position, item)
        }
        holder.view.setOnLongClickListener {
            val onItemLongClick = onItemLongClick
            if (onItemLongClick == null) {
                false
            } else {
                onItemLongClick.invoke(position, item)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<T> {
        val view = LayoutInflater.from(parent.context)
                .inflate(itemLayoutId, parent, false)
        return constructViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    abstract class ViewHolder<T>(val view: View) : RecyclerView.ViewHolder(view) {
        var item: T? = null
    }

    fun filter(predicate: (T) -> Boolean) {
        val newList = mutableListOf<T>()

        for (item in items) {
            if (predicate(item)) newList.add(item)
        }

        items = newList
    }
}
