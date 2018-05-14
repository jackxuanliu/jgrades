package website.jackl.jgrades.protocol.request.aeries

import android.util.Log
import com.android.volley.Request
import com.android.volley.VolleyError
import org.json.JSONObject
import website.jackl.jgrades.protocol.ServerError
import website.jackl.jgrades.protocol.request.aeries.volley.AeriesJsonRequest

/**
 * Created by jack on 12/26/17.
 */
class IsUserLoggedInRequest(val onResult: (Boolean) -> Unit, onError: (ServerError) -> Unit) : AeriesRequest(onError) {

    override fun constructRequest(service: ServiceInterface): Request<out Any> {
        return AeriesJsonRequest(Request.Method.GET, service.constructUrl(), null, this::onVolleyResult, this::onVolleyError)
    }

    private fun onVolleyResult(response: JSONObject?) {
        val isLoggedIn = response!!.optBoolean("d", false)
        onResult(isLoggedIn)
    }

    private fun onVolleyError(error: VolleyError?) {
        Log.d("error", "volleyError")
        onError(ServerError.CONNECTION)
    }

    override val requestPath = "IsUserLoggedIn"

}