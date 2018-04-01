package website.jackl.jgrades.protocol.request.aeries.volley

import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import org.json.JSONObject

/**
 * Created by jack on 12/28/17.
 */
class AeriesJsonRequest(method: Int, url: String, json: JSONObject?, listener: (JSONObject?) -> Unit, errorListener: (VolleyError?) -> Unit) : JsonObjectRequest(method, url, json, listener, errorListener) {

    override fun getHeaders(): MutableMap<String, String> { // Aeries requires these headers for some reason when doing JSON requests
        return mutableMapOf(Pair("Content-Type", "application/json; charset=utf-8"))
    }
}