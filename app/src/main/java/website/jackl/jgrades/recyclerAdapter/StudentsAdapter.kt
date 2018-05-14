package website.jackl.jgrades.recyclerAdapter

import android.view.View
import android.widget.TextView
import website.jackl.jgrades.Data.Student
import website.jackl.jgrades.R

/**
 * Created by jack on 2/18/18.
 */
class StudentsAdapter : MyListAdapter<Student.Info>() {
    override val itemLayoutId: Int = R.layout.listitem_student
    override val emptyTitleId: Int = R.string.emptyTitle_students


    override fun constructViewHolder(view: View): MyListAdapter.ViewHolder<Student.Info> {
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyListAdapter.ViewHolder<Student.Info>, position: Int) {
        super.onBindViewHolder(holder, position)

        if (holder is ViewHolder) {
            val student = items[position]
            holder.name.text = student.nameOfStudent
            holder.school.text = student.nameOfSchool
        }
    }

    class ViewHolder(itemView: View) : MyListAdapter.ViewHolder<Student.Info>(itemView) {
        val name: TextView
        val school: TextView
        val parent: View

        init {
            parent = itemView
            school = itemView.findViewById(R.id.student_school)
            name = itemView.findViewById(R.id.student_name)
        }

//        init {
//            parent.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
//                override fun onGlobalLayout() {
//                    name.maxWidth = parent.width - (parent.paddingStart * 2)
//                    school.maxWidth = parent.width - (parent.paddingStart * 2)
//                }
//            })
//        }

    }

}