package website.jackl.jgrades.protocol.request.aeries

import com.android.volley.Request
import website.jackl.jgrades.Data.Gradebook
import website.jackl.jgrades.protocol.ServerError

/**
 * Created by jack on 2/4/18.
 */
class GradebookDetailsRequest(val numberTerm: String, val onResult: (Result<Gradebook.Details>) -> Unit, onError: (ServerError) -> Unit) : StudentRequiredRequest(onError) {

    override fun performPostStudent(service: ServiceInterface) {
        this.service = service
        val detailedSummaryDataRequest = GradebookDetailedSummaryDataRequest(numberTerm, this::onDetailedSummaryDataResult, onError)
        detailedSummaryDataRequest.skipLogin = true
        detailedSummaryDataRequest.skipStudent = true
        detailedSummaryDataRequest.perform(service)
    }

    private var service: ServiceInterface? = null

    private fun onDetailedSummaryDataResult(detailedSummaryData: Gradebook.DetailedSummaryData) {
        val assignmentsRequest = GradebookAssignmentsRequest(numberTerm, {
            onResult(Result(Gradebook.Details(detailedSummaryData, it), student))
        }, onError)
        assignmentsRequest.skipLogin = true
        assignmentsRequest.skipStudent = true
        assignmentsRequest.perform(service!!)
    }

    override fun constructRequest(service: ServiceInterface): Request<out Any> {
        throw RuntimeException()
    }

    override val requestPath: String
        get() = throw RuntimeException()
}
