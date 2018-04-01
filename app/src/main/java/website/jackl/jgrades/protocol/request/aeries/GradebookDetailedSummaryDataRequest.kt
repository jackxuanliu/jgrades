package website.jackl.jgrades.protocol.request.aeries

import android.util.Log
import com.android.volley.Request
import com.android.volley.VolleyError
import org.json.JSONObject
import website.jackl.jgrades.Data.CollectionExtensions.jsonObjectIterator
import website.jackl.jgrades.Data.Gradebook
import website.jackl.jgrades.protocol.ServerError
import website.jackl.jgrades.protocol.request.ServerRequest
import website.jackl.jgrades.protocol.request.aeries.volley.AeriesJsonRequest

/**
 * Created by jack on 2/4/18.
 */
class GradebookDetailedSummaryDataRequest(val numberTerm: String, val onResult: (Gradebook.DetailedSummaryData) -> Unit, onError: (ServerError) -> Unit) : StudentRequiredRequest(onError) {
    override fun constructRequest(service: ServerRequest.ServiceInterface): Request<out Any> {
        throw RuntimeException()
    }

    override fun performPostStudent(service: ServerRequest.ServiceInterface) {
        this.service = service

        val requestJson = JSONObject()
        val parts = numberTerm.split("_")

        requestJson.put("gradebookNumber", parts[0])
        requestJson.put("term", parts[1])

        val first = AeriesJsonRequest(Request.Method.POST, service.constructUrl(), requestJson, this::onVolleyResult, this::onVolleyError)
        service.addRequest(first)
    }

    private var service: ServerRequest.ServiceInterface? = null

    private fun onVolleyResult(response: JSONObject?) {
        val results = response!!.getJSONObject("d").getJSONArray("results")

        var points: Double? = null
        var maxPoints: Double? = null
        val categories = mutableMapOf<String, Gradebook.Category>()

        for (result in results.jsonObjectIterator()) {
            if (result.getString("type") == "TOTAL") {
                points = result.getDouble("numberOfPoints")
                maxPoints = result.getDouble("maxPoints")
            } else {
                categories.put(result.getString("category"), Gradebook.Category(
                        result.getString("category"),
                        result.getDouble("numberOfPoints"),
                        result.getDouble("maxPoints"),
                        result.getDouble("percentOfGrade") / 100.0
                ))
            }
        }

        onResult(Gradebook.DetailedSummaryData(points!!, maxPoints!!, categories))

    }

    private fun onVolleyError(error: VolleyError?) {
        Log.d("error", "volleyError")
        onError(ServerError.CONNECTION)
    }

    override val requestPath: String = "GetGradebookDetailedSummaryData"
}