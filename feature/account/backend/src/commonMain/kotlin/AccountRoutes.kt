package app.vercors.feature.account.backend

import app.vercors.shared.backend.cache
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import kotlin.time.Duration.Companion.minutes

fun Route.accountRoutes() {
    route("/v1/account/{accountId}") {
        get("/avatar") {
            val accountId: String by call.parameters
            call.response.cache(1.minutes)
            call.respondRedirect("https://mc-heads.net/avatar/$accountId")
        }

        get("/head") {
            val accountId: String by call.parameters
            call.response.cache(1.minutes)
            call.respondRedirect("https://mc-heads.net/head/$accountId")
        }

        get("/body") {
            val accountId: String by call.parameters
            call.response.cache(1.minutes)
            call.respondRedirect("https://mc-heads.net/body/$accountId")
        }
    }
}
