package website.jackl.jgrades.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BaseTransientBottomBar
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.ViewGroup
import android.view.accessibility.AccessibilityManager
import android.view.inputmethod.InputMethodManager
import website.jackl.jgrades.Data.SessionData
import website.jackl.jgrades.Data.SettingsManager
import website.jackl.jgrades.GradesApplication
import website.jackl.jgrades.R
import website.jackl.jgrades.fragment.defaultPrefs
import website.jackl.jgrades.protocol.ServerError
import website.jackl.jgrades.protocol.service.ServerService
import website.jackl.jgrades.newStore

/**
 * Created by jack on 12/29/17.
 */
abstract class GradesActivity<ParentLayout : ViewGroup> : AppCompatActivity() {
    open fun onServiceReady() = Unit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        gradesApp = application as GradesApplication
        connection =  ServerService.Connection(this)

        store = newStore

        defaultPrefs.getString("pref_theme", null)?.apply {
            setTheme(themeMap[this]!!)
        }

    }

    override fun onStart() {
        super.onStart()

        val global = newStore.loadGlobal()

        if (global.activeEmail != null) {
            val user = store.loadUser(global.activeEmail)
            connection.bindService(SessionData(
                    aeriesUrl = global.district?.url,
                    user = user
            ), this::onServiceConnected)
        } else {
            connection.bindService(SessionData(
                    aeriesUrl = global.district?.url
            ), this::onServiceConnected)
        }
    }

    override fun onStop() {
        super.onStop()

        connection.unbindService()
    }

    private fun onServiceConnected(binder: ServerService.Connection.Binder) {
        this.service = binder
        onServiceReady()
    }

    fun onErrorSnackbar(error: ServerError) {
        Log.d("error", error.toString())

        val message: Int = when (error) {

            ServerError.CONNECTION -> {
                 R.string.error_connection
            }
            ServerError.AUTHENTICATION -> {
                 R.string.error_authentication
            }
            ServerError.UNKNOWN -> {
                R.string.error_unknown
            }
            ServerError.STUDENT -> {
                throw RuntimeException() // TODO add student missing logic
            }
        }

        showSnackbar(message, Snackbar.LENGTH_SHORT)
    }

    fun showSnackbar(message: Int, duration:Int) {
        val snackbarView = coordinator ?: parent
        Snackbar.make(snackbarView, message, duration).apply {
            try {
                val mAccessibilityManagerField = BaseTransientBottomBar::class.java.getDeclaredField("mAccessibilityManager")
                mAccessibilityManagerField.isAccessible = true
                val accessibilityManager = mAccessibilityManagerField.get(this)
                val mIsEnabledField = AccessibilityManager::class.java.getDeclaredField("mIsEnabled")
                mIsEnabledField.isAccessible = true
                mIsEnabledField.setBoolean(accessibilityManager, false)
                mAccessibilityManagerField.set(this, accessibilityManager)
            } catch (e: Exception) {
                Log.d("Snackbar", "Reflection error: $e")
            }
        }.show()
    }

    fun hideKeyboard() {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = coordinator ?: parent
        inputManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

     fun launchDistrictSelection() {
        val intent = Intent(this, DistrictSelectionActivity::class.java)
        startActivity(intent)
    }

     fun launchLogin() {
         val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    fun launchMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun setPreferredTheme() {

    }

    protected var coordinator: CoordinatorLayout? = null
    protected lateinit var parent: ParentLayout

    lateinit var service: ServerService.Connection.Binder
    lateinit var connection: ServerService.Connection

    lateinit var store: SettingsManager

    var gradesApp: GradesApplication
    get() = gradesAppField!!
    set(value) {
        gradesAppField = value
    }

    private var gradesAppField: GradesApplication? = null

}

private val themeMap: Map<String, Int> = mapOf(
        Pair("cyan", R.style.AppTheme),
        Pair("red", R.style.AppTheme_Red),
        Pair("purple", R.style.AppTheme_Purple),
        Pair("deepPurple", R.style.AppTheme_DeepPurple),
        Pair("indigo", R.style.AppTheme_Indigo),
        Pair("teal", R.style.AppTheme_Teal),
        Pair("green", R.style.AppTheme_Green),
        Pair("orange", R.style.AppTheme_Orange),
        Pair("dark", R.style.AppTheme_Dark),
        Pair("black", R.style.AppTheme_Black)
)