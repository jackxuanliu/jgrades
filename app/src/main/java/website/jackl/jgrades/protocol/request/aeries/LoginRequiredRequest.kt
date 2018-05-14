package website.jackl.jgrades.protocol.request.aeries

import website.jackl.jgrades.protocol.ServerError

/**
 * Created by jack on 1/2/18.
 */

abstract class LoginRequiredRequest(onError: (ServerError) -> Unit) : AeriesRequest(onError) {
    var skipLogin = false

    open fun performPostLogin(service: ServiceInterface) {
        super.perform(service)
    }

    override final fun perform(service: ServiceInterface) {

        if (skipLogin) {
            performPostLogin(service)
        } else {
            val checkRequest = IsUserLoggedInRequest(
                    onResult = {
                        if (it) {
                            performPostLogin(service)
                        } else {
                            loginBeforeRequest(service) // user confirmed not logged in -> login before making request
                        }
                    },
                    onError = onError
            )
            checkRequest.perform(service)
        }
    }


    private fun loginBeforeRequest(service: ServiceInterface) {
        val user = service.sessionData.user
        val email = user?.email
        val password = user?.password
        if (email == null || password == null) {
            onError(ServerError.AUTHENTICATION)
        } else {
            val loginRequest = LoginRequest(
                    onResult = {
                        if (it) { // if login successful
                            performPostLogin(service)
                        } else {
                            onError(ServerError.AUTHENTICATION)
                        }
                    },
                    onError = {
                        onError(ServerError.CONNECTION) // unable to connect/login
                    })

            loginRequest.perform(service)
        }

    }

}