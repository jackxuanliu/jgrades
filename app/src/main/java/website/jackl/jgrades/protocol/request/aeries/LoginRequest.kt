package website.jackl.jgrades.protocol.request.aeries

import android.util.Log
import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.HttpHeaderParser
import website.jackl.jgrades.protocol.addUrlPath
import website.jackl.jgrades.protocol.ServerError

/**
 * Created by jack on 12/28/17.
 */
class LoginRequest(val onResult: (Boolean) -> Unit, onError: (ServerError) -> Unit) : AeriesRequest(onError) {
    class LoginRequest(val username: String, val password: String, url: String, val onResult: () -> Unit, val onError: (VolleyError) -> Unit) : Request<Unit>(Method.POST, url, onError)
    {
        override fun getParams(): MutableMap<String, String> =
                mutableMapOf(
                    Pair("checkCookiesEnabled", "true"),
                    Pair("checkMobileDevice", "true"),
                    Pair("checkStandaloneMode", "false"),
                    Pair("checkTabletDevice", "false"),
                    Pair("portalAccountUsername", username),
                    Pair("portalAccountPassword", password),
                    Pair("portalAccountUsernameLabel", ""),
                    Pair("submit",  "")
            )

        override fun deliverResponse(response: Unit?) {
            onResult()
        }

        override fun parseNetworkResponse(response: NetworkResponse?): Response<Unit> {
            return Response.success(Unit ,HttpHeaderParser.parseCacheHeaders(response))
        }


    }

    override fun constructRequest(service: ServiceInterface): Request<out Any> {
        throw RuntimeException()
    }

     override fun perform(service: ServiceInterface) {
         service.clearCookies() // clear previous session
         val user = service.sessionData.user
         val email = user?.email
         val password = user?.password
         if (email == null || password == null) {
             onError(ServerError.AUTHENTICATION)
             return
         }

         Log.d("session", service.sessionData.aeriesUrl!!.addUrlPath(requestPath))

         val request = LoginRequest(email, password, service.sessionData.aeriesUrl!!.addUrlPath(requestPath),
                 onResult = {
             checkLogin(service) // after login request, check if successful
         },
                 onError = this::onErrorResponse)
         service.addRequest(request)
     }

    private fun checkLogin(service: ServiceInterface) {
        val request = IsUserLoggedInRequest(onResult, onError)
        request.perform(service)
    }

    private fun onErrorResponse(error: VolleyError)
    {
        error.printStackTrace()
        onError(ServerError.CONNECTION)
    }

    override val requestPath: String = "LoginParent.aspx"

}