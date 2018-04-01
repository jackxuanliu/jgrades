package website.jackl.jgrades.protocol.request

import com.android.volley.Request
import website.jackl.jgrades.Data.SessionData
import website.jackl.jgrades.protocol.service.ServerService

/**
 * Created by jack on 1/2/18.
 */
abstract class ServerRequest() {
    interface ServiceInterface {
        fun addRequest(request: Request<out Any>)
        fun clearCookies()

        val sessionData: SessionData
    }



    open fun perform(service: ServiceInterface) {
        service.addRequest(constructRequest(service))
    }

    abstract fun constructRequest(service: ServiceInterface): Request<out Any>
}