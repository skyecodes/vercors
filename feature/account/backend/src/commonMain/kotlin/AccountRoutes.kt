package app.vercors.feature.account.backend

import app.vercors.feature.account.api.Accounts
import app.vercors.shared.backend.cache
import io.ktor.server.response.*
import io.ktor.server.routing.*
import opensavvy.spine.server.route
import kotlin.time.Duration.Companion.minutes

fun Route.accountRoutes() {
    route(Accounts.AccountId.avatar) {
        call.response.cache(1.minutes)
        call.respondRedirect("https://mc-heads.net/avatar/${idOf(Accounts.AccountId)}")
    }
    route(Accounts.AccountId.head) {
        call.response.cache(1.minutes)
        call.respondRedirect("https://mc-heads.net/head/${idOf(Accounts.AccountId)}")
    }
    route(Accounts.AccountId.body) {
        call.response.cache(1.minutes)
        call.respondRedirect("https://mc-heads.net/body/${idOf(Accounts.AccountId)}")
    }
}
