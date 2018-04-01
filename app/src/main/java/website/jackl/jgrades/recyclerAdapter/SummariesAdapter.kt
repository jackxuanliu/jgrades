package website.jackl.jgrades.recyclerAdapter

import android.graphics.Typeface
import android.support.v4.graphics.TypefaceCompat
import android.support.v4.graphics.TypefaceCompatUtil
import android.view.View
import android.view.ViewTreeObserver
import android.widget.TextView
import website.jackl.jgrades.R
import website.jackl.jgrades.Data.Gradebook

/**
 * Created by jack on 1/28/18.
 */
class SummariesAdapter : MyListAdapter<Gradebook> (){
    override val itemLayoutId: Int = R.layout.listitem_summary

    override val emptyTitleId: Int = R.string.emptyTitle_classes

    override fun constructViewHolder(view: View): ViewHolder {
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyListAdapter.ViewHolder<Gradebook>, position: Int) {
        super.onBindViewHolder(holder, position)

        val item = items[position]

        if (holder is ViewHolder) {
            holder.name.text = item.summary.name
            holder.percent.text = item.summary.officialPercent.toString() + "%"
            holder.mark.text = item.summary.officialMark
            holder.term.text = item.summary.term

            if (item.lastView != null && item.lastView < item.summary.lastUpdated) {
                holder.name.setTypeface(null, Typeface.BOLD)
                holder.percent.setTypeface(null, Typeface.BOLD)
            } else {
                holder.name.setTypeface(null, Typeface.NORMAL)
                holder.percent.setTypeface(null, Typeface.NORMAL)
            }

        }

    }

    class ViewHolder(view: View) : MyListAdapter.ViewHolder<Gradebook>(view) {
        val name: TextView
        val percent: TextView
        val term: TextView
        val mark: TextView

        init {
            name = view.findViewById(R.id.gradebookSummary_name) as TextView
            percent = view.findViewById(R.id.gradebookSummary_percent) as TextView
            term = view.findViewById(R.id.gradebookSummary_term) as TextView
            mark = view.findViewById(R.id.gradebookSummary_mark) as TextView
        }
        // TODO implement new TextView resizing thing
//        init {
//            val parent = view
//            parent.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
//                override fun onGlobalLayout() {
//                    name.maxWidth = parent.width - (parent.paddingStart * 3) - percent.width
//                    term.maxWidth = parent.width - (parent.paddingStart * 3) - mark.width
//                }
//            })
//        }


    }
}