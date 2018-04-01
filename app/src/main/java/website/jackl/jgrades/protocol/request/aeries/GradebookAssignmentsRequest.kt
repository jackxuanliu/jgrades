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
 * Created by jack on 1/28/18.
 */
class GradebookAssignmentsRequest(val numberTerm: String, val onResult: (List<Gradebook.Assignment>) -> Unit, onError: (ServerError) -> Unit) : StudentRequiredRequest(onError) {
    override fun constructRequest(service: ServerRequest.ServiceInterface): Request<out Any> {
        throw RuntimeException()
    }

    override fun performPostStudent(service: ServiceInterface) {
        this.service = service

        val requestJson = JSONObject()
        val parts = numberTerm.split("_")

        requestJson.put("gradebookNumber", parts[0])
        requestJson.put("term", parts[1])
        requestJson.put("requestedPage", 1)
        requestJson.put("pageSize", 0)



        val first = AeriesJsonRequest(Request.Method.POST, service.constructUrl(), requestJson, this::onFirstVolleyResult, this::onVolleyError)
        service.addRequest(first)
    }

    private var service: ServiceInterface? = null

    private fun onFirstVolleyResult(response: JSONObject?) {
        val requestJson = JSONObject()
        val parts = numberTerm.split("_")

        requestJson.put("gradebookNumber", parts[0])
        requestJson.put("term", parts[1])
        requestJson.put("requestedPage", 1)
        requestJson.put("pageSize", response!!.getJSONObject("d").getInt("total"))

        val second = AeriesJsonRequest(Request.Method.POST, service!!.constructUrl(), requestJson, this::onSecondVolleyResult, this::onVolleyError)
        service!!.addRequest(second)
    }

    private fun onSecondVolleyResult(response: JSONObject?) {
        val gradebookArray = response?.optJSONObject("d")?.optJSONArray("results")
        val gradebookList = mutableListOf<Gradebook.Assignment>()
        if (gradebookArray != null) {
            for (item in gradebookArray.jsonObjectIterator()) {
                try {
                    gradebookList.add(
                            Gradebook.Assignment(item.getString("description"),
                                    item.getString("type"),
                                    item.getDouble("score"),
                                    item.getDouble("maxScore"),
                                    item.getBoolean("isGraded"))
                    )
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        }
        onResult(gradebookList)
    }

    private fun onVolleyError(error: VolleyError?) {
        Log.d("error", "volleyError")
        onError(ServerError.CONNECTION)
    }

    override val requestPath: String = "GetGradebookDetailsData"
}