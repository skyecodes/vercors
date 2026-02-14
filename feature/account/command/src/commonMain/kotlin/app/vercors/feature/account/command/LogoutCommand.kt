package app.vercors.feature.account.command

import app.vercors.feature.account.domain.usecase.LogoutUseCase
import app.vercors.shared.command.AbstractCommand
import app.vercors.shared.command.CommandError
import arrow.core.raise.Raise
import arrow.core.raise.context.withError
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.validate
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import kotlin.uuid.Uuid

@ContributesIntoSet(AppScope::class)
class LogoutCommand(private val logout: LogoutUseCase) : AbstractCommand("logout") {
    val uuid by argument(help = "Account UUID").validate {
        require(it.isNotEmpty()) { "Account UUID cannot be empty" }
    }

    context(_: Raise<CommandError>)
    override suspend fun execute() {
        val account =
            withError({ CommandError("Failed to logout from account: ${it.message}") }) { logout(Uuid.parse(uuid)) }
        echo("Logged out of ${account.username} (${account.id.toHexString()})")
    }

    override fun help(context: Context): String = "Log out of your Microsoft account"
}
