package website.jackl.jgrades.activity

import android.os.Bundle
import android.os.Handler
import android.support.constraint.ConstraintLayout
import android.transition.Fade
import android.transition.TransitionManager
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.analytics.FirebaseAnalytics
import website.jackl.jgrades.R
import website.jackl.jgrades.Data.User
import website.jackl.jgrades.protocol.ServerError
import website.jackl.jgrades.protocol.request.aeries.IsUserLoggedInRequest
import website.jackl.jgrades.protocol.request.aeries.LoginRequest
import website.jackl.jgrades.newStore

class GreeterActivity : GradesActivity<ConstraintLayout>() {

    private lateinit var firebaseAnalytics: FirebaseAnalytics;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_greeter)

        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        coordinator = findViewById(R.id.coordinator)
        parent = findViewById(R.id.parent)

        statusHolder = findViewById(R.id.greeter_statusHolder)

        greeterText = findViewById(R.id.greeter_text)
        greeterText!!.typeface = gradesApp.shadows

        if (savedInstanceState == null) {
            parent!!.visibility = View.INVISIBLE
            Handler().postDelayed(this::fadeIn, 650)
        }

        MobileAds.initialize(this, "ca-app-pub-9033612152527375~1825525058")

    }

    override fun onStart() {
        super.onStart()

        inflateProgressView()
    }

    override fun onServiceReady() {
        connect()
    }

    override fun onStop() {
        super.onStop()
    }

    fun fadeIn()
    {
        TransitionManager.beginDelayedTransition(parent, Fade())
        parent!!.visibility = View.VISIBLE
    }

    private fun connect() {
        val global = newStore.loadGlobal()
        val districtUrl = global.district?.url

        if (global.activeEmail != null) {
            val user = store.loadUser(global.activeEmail)
            if (user.password != null) {
                if (store.loadAnyGradebookSummaries().result.isNotEmpty()) {
                    launchMainActivity()
                    return
                }
            }
        }

        if (districtUrl == null) {
            launchDistrictSelection()
        } else {
            val loginCheck = IsUserLoggedInRequest(this::onLoginCheck, this::onError)
            service.request(loginCheck)
        }
    }

    private fun onLoginCheck(loggedIn: Boolean) {
        if (loggedIn) {
            launchMainActivity()
        }
        else {
            val global = newStore.loadGlobal()

            val activeEmail = global.activeEmail

            val user = if (activeEmail != null) newStore.loadUser(activeEmail) else null
            val email = user?.email
            val password = user?.password

            if (email == null || password == null) { // somehow unable to retrieve emailField or passwordField
                launchLogin()
            } else { // BOTH emailField and passwordField retrieved
                connection.changeSessionData(connection.sessionData.copy(
                        user = User(email, password)
                ))

                val loginRequest = LoginRequest(this::onLogin, this::onError)
                service.request(loginRequest)

            }
        }
    }

    private fun onLogin(success: Boolean) {
        if (success) {
            launchMainActivity()
        }
        else {
            launchLogin()
        }
    }

    private fun onError(error: ServerError) {
        onErrorSnackbar(error)
        inflateErrorView()
    }

    private fun inflateProgressView() {
        val statusHolder = statusHolder!!

        statusHolder.removeAllViews()
        layoutInflater.inflate(R.layout.activity_greeter_progress, statusHolder)
    }

    private fun inflateErrorView() {
        val statusHolder = statusHolder!!

        statusHolder.removeAllViews()
        layoutInflater.inflate(R.layout.activity_greeter_error, statusHolder)

        val retryButton: Button = statusHolder.findViewById(R.id.greeterError_retryButton)
        val switchDistrictButton: Button = statusHolder.findViewById(R.id.greeterError_switchDistrictButton)

        retryButton.setOnClickListener({
            inflateProgressView()
            connect()
        })

        switchDistrictButton.setOnClickListener {
            launchDistrictSelection()
        }
    }

    var statusHolder: FrameLayout? = null
    var greeterText: TextView? = null



}
