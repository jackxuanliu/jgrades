package website.jackl.jgrades.protocol.request.aeries

import android.util.Log
import com.android.volley.Request
import com.android.volley.VolleyError
import org.json.JSONObject
import website.jackl.jgrades.Data.CollectionExtensions.jsonObjectIterator
import website.jackl.jgrades.Data.Gradebook
import website.jackl.jgrades.protocol.ServerError
import website.jackl.jgrades.protocol.request.aeries.volley.AeriesJsonRequest

/**
 * Created by jack on 1/20/18.
 */
class GradebookSummariesRequest(val onResult: (Result<List<Gradebook.Summary>>) -> Unit, onError: (ServerError) -> Unit) : StudentRequiredRequest(onError) {
    override fun constructRequest(service: ServiceInterface): Request<out Any> {
        return AeriesJsonRequest(Request.Method.GET, service.constructUrl(), null, this::onVolleyResult, this::onVolleyError)
    }

    private fun onVolleyResult(response: JSONObject?) {
        val gradebookArray = response?.optJSONObject("d")?.optJSONArray("results")
        val gradebookList = mutableListOf<Gradebook.Summary>()
        if (gradebookArray != null) {
            for (item in gradebookArray.jsonObjectIterator()) {
                try {
                    gradebookList.add(
                            Gradebook.Summary(
                                    item.getString("gradebookNumberTerm"),
                                    Gradebook.simplifyName(item.getString("className")),
                                    Gradebook.convertTerm(item.getString("term")),
                                    item.getInt("percentGrade"),
                                    item.getString("mark"),
                                    item.getString("code"),
                                    item.getString("updated").replace(Regex("\\D"), "").toLong()
                            )
                    )
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        }
        onResult(Result(gradebookList, student))
    }

    private fun onVolleyError(error: VolleyError?) {
        Log.d("error", "volleyError")
        onError(ServerError.CONNECTION)
    }

    override val requestPath: String = "GetGradebookSummaryData"
}