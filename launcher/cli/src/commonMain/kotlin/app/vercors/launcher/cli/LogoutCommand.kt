package app.vercors.launcher.cli

import app.vercors.feature.account.domain.usecase.LogoutUseCase
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.validate

class LogoutCommand : VercorsCommandBase("logout") {
    val uuidOrUsername by argument(help = "Account UUID or username").validate {
        require(it.isNotEmpty()) { "Account UUID or username cannot be empty" }
    }

    override suspend fun execute() {
        val logout = koin.get<LogoutUseCase>()
        val account = logout(uuidOrUsername)
        echo("Logged out of ${account.username} (${account.id.toHexString()})")
    }

    override fun help(context: Context): String = "Log out of your Microsoft account"
}
