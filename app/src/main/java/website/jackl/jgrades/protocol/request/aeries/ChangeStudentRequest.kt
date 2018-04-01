package website.jackl.jgrades.protocol.request.aeries

import com.android.volley.Request
import org.json.JSONObject
import website.jackl.jgrades.Data.Student
import website.jackl.jgrades.protocol.ServerError
import website.jackl.jgrades.protocol.request.aeries.volley.AeriesJsonRequest


/**
 * Created by jack on 1/26/18.
 */

class ChangeStudentRequest(val student: Student.Info, val onResult: () -> Unit, onError: (ServerError) -> Unit) : LoginRequiredRequest(onError) {
    override fun constructRequest(service: ServiceInterface): Request<out Any> {
        val requestJson = JSONObject()
        requestJson.put("schoolCode", student.schoolCode)
        requestJson.put("studentNumber", student.studentNumber)
        return AeriesJsonRequest(Request.Method.POST, service.constructUrl(), requestJson, this::onVolleyResult, this::onVolleyErrorDefault)
    }

    private fun onVolleyResult(jsonObject: JSONObject?) {
        onResult()
    }

    override val requestPath: String = "ChangeToStudent"
}