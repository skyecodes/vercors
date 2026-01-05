package app.vercors.launcher.cli

import app.vercors.feature.account.domain.usecase.LogoutUseCase
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.validate
import kotlin.uuid.Uuid

class LogoutCommand(private val logout: LogoutUseCase) : VercorsCommandBase("logout") {
    val uuid by argument(help = "Account UUID").validate {
        require(it.isNotEmpty()) { "Account UUID cannot be empty" }
    }

    override suspend fun execute() {
        val account = logout(Uuid.parse(uuid))
        echo("Logged out of ${account.username} (${account.id.toHexString()})")
    }

    override fun help(context: Context): String = "Log out of your Microsoft account"
}
