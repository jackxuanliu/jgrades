package website.jackl.jgrades.recyclerAdapter

import android.support.constraint.ConstraintLayout
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import website.jackl.jgrades.Data.Gradebook
import website.jackl.jgrades.R
import kotlin.math.roundToInt

/**
 * Created by jack on 1/28/18.
 */

class AssignmentsAdapter : MyListAdapter<Gradebook.Assignment>() { // TODO
    var sliderPosition: Int? = null
    var onSlid: (() -> Unit)? = null

    override val itemLayoutId: Int = R.layout.listitem_assignment
    override val emptyTitleId: Int = R.string.emptyTitle_assignments

    override fun constructViewHolder(view: View): MyListAdapter.ViewHolder<Gradebook.Assignment> {
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyListAdapter.ViewHolder<Gradebook.Assignment>, position: Int) {
        super.onBindViewHolder(holder, position)

        if (holder is ViewHolder) {
            val assignment = items[position]
            holder.apply {
                name.text = assignment.name
                percent.text = assignment.readablePercent
                fraction.text = assignment.readableFraction
                category.text = assignment.category

                if (assignment.isGraded) {
                    enableEverything(this)
                } else {
                    disableEverything(this)
                }

                if (position == sliderPosition) {
                    slider.setOnSeekBarChangeListener(null)
                    slider.visibility = View.VISIBLE
                    slider.max = Math.floor(assignment.maxPoints).toInt()
                    slider.progress = assignment.points.roundToInt()
                    slider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(p0: SeekBar?, p1: Int, fromUser: Boolean) {
                            if (fromUser) {
                                val newAssignment = assignment.copy(points = p1.toDouble(), isGraded = true)
                                items.set(position, newAssignment)
                                fraction.text = newAssignment.readableFraction
                                percent.text = newAssignment.readablePercent
                                enableEverything(this@apply)
                            }
                            onSlid?.invoke()
                        }

                        override fun onStartTrackingTouch(p0: SeekBar?) {

                        }

                        override fun onStopTrackingTouch(p0: SeekBar?) {
                        }
                    })
                } else {
                    slider.visibility = View.GONE
                }
            }

        }
    }

    private fun enableEverything(holder: ViewHolder) {
        holder.name.isEnabled = true
        holder.percent.isEnabled = true
        holder.fraction.isEnabled = true
        holder.category.isEnabled = true
    }

    private fun disableEverything(holder: ViewHolder) {
        holder.name.isEnabled = false
        holder.percent.isEnabled = false
        holder.fraction.isEnabled = false
        holder.category.isEnabled = false
    }

    class ViewHolder(itemView: View) : MyListAdapter.ViewHolder<Gradebook.Assignment>(itemView) {
        val name: TextView = itemView.findViewById(R.id.assignment_name)
        val percent: TextView = itemView.findViewById(R.id.assignment_percent)
        val category: TextView = itemView.findViewById(R.id.assignment_category)
        val fraction: TextView = itemView.findViewById(R.id.assignment_fraction)
        val slider: SeekBar = itemView.findViewById(R.id.assignment_scoreSlider)
        val parent: ConstraintLayout = itemView.findViewById(R.id.parent)
//        init {
//            parent.viewTreeObserver.addOnGlobalLayoutListener({
//                val nameMaxWidth = parent.width - (parent.paddingStart * 3) - percent.width
//                val nameMeasuredWidth = name.measuredWidth
//                if (nameMeasuredWidth > nameMaxWidth) {
//                    Log.d(name.text.toString(), nameMeasuredWidth.toString())
//
//                    name.width = nameMaxWidth
//                } else {
//                    Log.d(name.text.toString(), nameMeasuredWidth.toString())
//
//                    name.layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
//                }
//            })
//
//        }
    }

    val Gradebook.Assignment.readableFraction: String
        get() {
            val formattedPoints = Gradebook.decimalFormat.format(points)
            val formattedMaxPoints = Gradebook.decimalFormat.format(maxPoints)

            if (points == 0.00 && !isGraded) {
                return "\u200E" + "N/A" + " / " + formattedMaxPoints
            } else {
                return "\u200E" + formattedPoints + " / " + formattedMaxPoints
            }
        }

    val Gradebook.Assignment.readablePercent: String
        get() {
            if (points == 0.00 && !isGraded) {
                return "N/A"
            } else {
                return Gradebook.percentFormat.format(points / maxPoints)
            }
        }
}