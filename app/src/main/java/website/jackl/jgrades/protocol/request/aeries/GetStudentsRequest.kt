package website.jackl.jgrades.protocol.request.aeries

import com.android.volley.Request
import org.json.JSONObject
import website.jackl.jgrades.Data.CollectionExtensions.jsonObjectIterator
import website.jackl.jgrades.Data.Student
import website.jackl.jgrades.protocol.ServerError
import website.jackl.jgrades.protocol.request.aeries.volley.AeriesJsonRequest

/**
 * Created by jack on 1/26/18.
 */
class
GetStudentsRequest(val onResult: (List<Student.Info>, Student.Info) -> Unit, onError: (ServerError) -> Unit) : LoginRequiredRequest(onError) {

    override fun constructRequest(service: ServiceInterface): Request<out Any> {
        return AeriesJsonRequest(Request.Method.GET, service.constructUrl(), null, this::onVolleyResult, this::onVolleyErrorDefault)
    }

    private fun onVolleyResult(json: JSONObject?) {
        val studentsArray = json!!.getJSONObject("d").getJSONArray("results")
        val students = mutableListOf<Student.Info>()

        var currentStudent: Student.Info? = null
        for (student in studentsArray.jsonObjectIterator()) {
            val info = Student.Info(
                    student.getString("schoolCode"),
                    student.getString("studentNumber"),
                    student.getString("nameOfStudent"),
                    student.getString("nameOfSchool")
            )
            students.add(info)

            if (student.getBoolean("isCurrentStudent")) {
                if (currentStudent != null) throw RuntimeException()
                currentStudent = info
            }

        }

        onResult(students, currentStudent!!)
    }

    override val requestPath: String = "GetStudentsOfCurrentAccount"
}