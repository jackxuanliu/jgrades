package website.jackl.jgrades.recyclerAdapter

import android.view.View
import android.widget.TextView
import website.jackl.jgrades.Data.District
import website.jackl.jgrades.R

/**
 * Created by jack on 12/30/17.
 */

class DistrictsAdapter : MyListAdapter<District>() {
    var onMissingDistrict: (() -> Unit)? = null

    override val itemLayoutId: Int = R.layout.recycler_districts_item
    override val emptyTitleId: Int = R.string.emptyTitle_districts


    override fun constructViewHolder(view: View): MyListAdapter.ViewHolder<District> {
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + 1
    }

    override fun onBindViewHolder(holder: MyListAdapter.ViewHolder<District>, position: Int) {


        if (holder is ViewHolder) {
            if (position == 0) {
                holder.name.text = "Missing / Broken district..."
                holder.view.setOnClickListener({ onMissingDistrict?.invoke() })
            } else {
                val item = items[position - 1]
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
                holder.name.text = item.name
            }
        }
    }


    class ViewHolder(itemView: View) : MyListAdapter.ViewHolder<District>(itemView) {
        val name: TextView
        val parent: View

        init {
            parent = itemView
            name = itemView.findViewById(R.id.recyclerDistrictsItem_name)
        }

//        init {
//            parent.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
//                override fun onGlobalLayout() {
//                    name.maxWidth = parent.width - (parent.paddingStart * 2)
//                }
//            })
//        }

    }
}