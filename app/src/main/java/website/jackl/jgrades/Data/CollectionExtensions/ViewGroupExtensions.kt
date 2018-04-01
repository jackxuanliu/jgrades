package website.jackl.jgrades.Data.CollectionExtensions

import android.view.View
import android.view.ViewGroup

/**
 * Created by jack on 12/30/17.
 */

operator fun ViewGroup.iterator() = object : Iterator<View> {
    override fun hasNext() = index < childCount

    override fun next() = getChildAt(index++)

    private var index = 0
}