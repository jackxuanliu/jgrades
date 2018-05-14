package website.jackl.jgrades.protocol.request.backend

import website.jackl.jgrades.protocol.ServerError
import website.jackl.jgrades.protocol.request.ServerRequest

/**
 * Created by jack on 1/2/18.
 */
abstract class BackendRequest(onError: (ServerError) -> Unit) : ServerRequest() {
    abstract val requestPath: String
    val serverUrl = "https://www.example.com"
}