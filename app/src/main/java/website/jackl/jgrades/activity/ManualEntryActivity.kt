package website.jackl.jgrades.activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.constraint.ConstraintLayout
import android.support.design.widget.Snackbar
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import website.jackl.jgrades.Data.District
import website.jackl.jgrades.R
import website.jackl.jgrades.protocol.request.aeries.IsUserLoggedInRequest

class ManualEntryActivity : GradesActivity<ConstraintLayout>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual_entry)

        parent = findViewById(R.id.parent)
        url = findViewById(R.id.manualEntry_Url)
        connect = findViewById(R.id.manualEntry_connect)
        connect.setOnClickListener { onConnectButton() }
        progress = findViewById(R.id.manualEntry_progress)

        url.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    onConnectButton()
                    return true
                }
                return false
            }
        })
    }

    private fun onConnectButton() {
        progress.visibility = View.VISIBLE
        val manualDistrict = District("(Manually Entered District)", null, processUrl(url.text.toString()))
        val global = store.loadGlobal()
        store.saveGlobal(global.copy(district = manualDistrict))
        connection.bindService(connection.sessionData.copy(aeriesUrl = manualDistrict.url),
                {
                    this.service = it
                        val testRequest = IsUserLoggedInRequest({
                            progress.visibility = View.GONE
                            launchLogin()
                        }, {
                            progress.visibility = View.GONE
                            showSnackbar(R.string.manualEntry_error, Snackbar.LENGTH_LONG)
                        })
                        service.request(testRequest)

                })


        hideKeyboard()

    }

    private fun processUrl(url: String): String {
        val u1 = url.trim()
                .replace("LoginParent.aspx?page=default.aspx", "", true)
                .replace("http://", "", true)



        val u2 = if (u1.endsWith("/")) u1 else u1 + "/"
        val u3 = if (u2.startsWith("https://", true)) u2 else "https://" + u2

        return u3

    }

    private lateinit var progress: ProgressBar
    private lateinit var url: EditText
    private lateinit var connect: Button

}
