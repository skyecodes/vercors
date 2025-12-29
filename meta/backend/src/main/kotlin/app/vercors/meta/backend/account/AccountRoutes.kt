package app.vercors.meta.backend.account

import app.vercors.meta.backend.cache
import app.vercors.meta.backend.getValue
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlin.time.Duration.Companion.minutes

fun Route.accountRoutes() {
    route("/account/{accountId}") {
        route("/avatar") {
            getUserAvatar()
        }

        route("/head") {
            getUserHead()
        }

        route("/body") {
            getUserBody()
        }
    }
}

fun Route.getUserAvatar() {
    cache(5.minutes) {
        get {
            val accountId by call.parameters
            call.respondRedirect("https://mc-heads.net/avatar/$accountId")
        }
    }
}

fun Route.getUserHead() {
    cache(5.minutes) {
        get {
            val accountId by call.parameters
            call.respondRedirect("https://mc-heads.net/head/$accountId")
        }
    }
}

fun Route.getUserBody() {
    cache(5.minutes) {
        get {
            val accountId by call.parameters
            call.respondRedirect("https://mc-heads.net/body/$accountId")
        }
    }
}
