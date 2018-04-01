package website.jackl.jgrades.activity

import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.design.widget.Snackbar
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import website.jackl.jgrades.R
import website.jackl.jgrades.protocol.request.aeries.LoginRequest
import website.jackl.jgrades.newStore

class LoginActivity : GradesActivity<ConstraintLayout>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        coordinator = findViewById(R.id.coordinator)
        parent = findViewById(R.id.parent)

        emailField = findViewById(R.id.login_email)

        passwordField = findViewById(R.id.login_password)
        district = findViewById(R.id.login_district)

        val activeEmail = newStore.loadGlobal().activeEmail

        loginButton = findViewById(R.id.login_loginButton)
        switchDistrictButton = findViewById(R.id.login_switchDistrictsButton)

        splashText = findViewById(R.id.login_splashText)
        splashText!!.typeface = gradesApp.shadows

        loginButton!!.setOnClickListener(this::onLoginButton)
        passwordField!!.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    onLoginButton(null)
                    return true
                }
                return false
            }
        })

        switchDistrictButton!!.setOnClickListener { launchDistrictSelection() }

    }

    override fun onStart() {
        super.onStart()
        val global = newStore.loadGlobal()
        val name = global.district?.name
        if (name != null) {

            district.text = name
            district.visibility = View.VISIBLE
        } else {
            district.visibility = View.GONE
        }
    }

    override fun onBackPressed() { // only register back when emailField and passwordField are stored
        val global = newStore.loadGlobal()
        val activeEmail = global.activeEmail

        if (activeEmail == null) return

        val user = newStore.loadUser(activeEmail)
        val email = user.email
        val password = user.password

        if (email != null && password != null) {
            super.onBackPressed()
        }
    }

    private fun onLoginButton(view: View?) {
        hideKeyboard()

        var global = newStore.loadGlobal()

        val districtUrl = global.district?.url
        if (districtUrl == null) {
            launchDistrictSelection()
            return
        }

        val email = emailField!!.text?.trim()
        val password = passwordField!!.text

        if (email.isNullOrEmpty()|| password.isNullOrEmpty()) {
            showSnackbar(R.string.login_blankEmailOrPassword, Snackbar.LENGTH_SHORT)
        } else {
            val email = email.toString()
            global = global.copy(activeEmail = email)

            var user = newStore.loadUser(email).copy(password = password.toString())

            connection.changeSessionData(connection.sessionData.copy(
                    user = user
            ))

            val loginRequest = LoginRequest(this::onLogin, this::onErrorSnackbar)
            service.request(loginRequest)

            newStore.saveUser(user)
            newStore.saveGlobal(global)
        }
    }

    private fun onLogin(isSuccess: Boolean) {
        if (isSuccess) {
            Log.d("LoginRequest Status", "Success")
            launchMainActivity()
        } else {
            Log.d("LoginRequest Status", "Invalid Credentials")
            showSnackbar(R.string.login_incorrectEmailPasswordDistrict, Snackbar.LENGTH_SHORT)
        }
    }

    private lateinit var district: TextView
    var splashText: TextView? = null

    var emailField: EditText? = null
    var passwordField: EditText? = null

    var loginButton: Button? = null
    var switchDistrictButton: Button? = null
}
