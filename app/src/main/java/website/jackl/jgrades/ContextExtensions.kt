package website.jackl.jgrades

import android.content.Context
import website.jackl.jgrades.Data.SettingsManager

/**
 * Created by jack on 1/8/18.
 */

val Context.newStore: SettingsManager
    get() {
        val store = SettingsManager(this)
        return store
    }
