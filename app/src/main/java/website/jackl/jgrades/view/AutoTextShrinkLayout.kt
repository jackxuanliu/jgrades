package website.jackl.jgrades.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView

/**
 * Created by jack on 2/19/18.
 */

class AutoTextShrinkLayout(context: Context, attributeSet: AttributeSet) : LinearLayout(context, attributeSet) {
    var initialized = false
    lateinit var first: TextView
    lateinit var second: TextView

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        if (initialized) {
            val maxWidth = (r - l) - (paddingStart * 3) - second.width
            Log.d("MeasuredWidth", first.measuredWidth.toString())
            Log.d("MaxWidth", maxWidth.toString())

            if (first.measuredWidth > maxWidth) {
                first.width = maxWidth
            } else {
                first.width = ViewGroup.LayoutParams.WRAP_CONTENT
            }
        }
    }

    override fun onViewAdded(child: View?) {
        super.onViewAdded(child)
        if (childCount == 2) {
            first = getChildAt(0) as TextView
            second = getChildAt(1) as TextView
            initialized = true
        } else if (childCount > 2) {
            throw RuntimeException()
        }
    }
}