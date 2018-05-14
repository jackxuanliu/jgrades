package website.jackl.jgrades

import android.app.Application
import android.graphics.Typeface

/**
 * Created by jack on 1/7/18.
 */

class GradesApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        shadows = Typeface.createFromAsset(assets, "fonts/shadows.ttf")
    }

    var shadows: Typeface
        get() {
            return shadowsField!!
        }
        set(value) {
            shadowsField = value
        }

    private var shadowsField: Typeface? = null

}