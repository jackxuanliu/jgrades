package website.jackl.jgrades.protocol.request.aeries

import android.util.Log
import com.android.volley.VolleyError
import website.jackl.jgrades.protocol.addUrlPath
import website.jackl.jgrades.protocol.ServerError
import website.jackl.jgrades.protocol.request.ServerRequest

/**
 * Created by jack on 12/21/17.
 */
abstract class AeriesRequest(val onError: (ServerError) -> Unit) : ServerRequest() {
    fun ServiceInterface.constructUrl(): String {
        return sessionData.aeriesUrl!!.addUrlPath(mobileApiPath).addUrlPath(requestPath)
    }

    fun getRequestUrl(url: String): String{
        return url.addUrlPath(mobileApiPath).addUrlPath(requestPath)
    }

    protected fun onVolleyErrorDefault(error: VolleyError?) {
        Log.d("error", "volleyError")
        onError(ServerError.CONNECTION)
    }

    val mobileApiPath = "m/api/MobileWebAPI.asmx"
    abstract val requestPath: String
}