package app.vercors.feature.account.backend

import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*

fun Route.accountRoutes() {
    route("/account/{accountId}") {
        get("/avatar") {
            val accountId: String by call.parameters
            call.respondRedirect("https://mc-heads.net/avatar/$accountId")
        }

        get("/head") {
            val accountId: String by call.parameters
            call.respondRedirect("https://mc-heads.net/head/$accountId")
        }

        get("/body") {
            val accountId: String by call.parameters
            call.respondRedirect("https://mc-heads.net/body/$accountId")
        }
    }
}
