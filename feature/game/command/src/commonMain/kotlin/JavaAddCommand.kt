package app.vercors.feature.game.command

import app.vercors.feature.game.domain.model.JavaInstallationError
import app.vercors.feature.game.domain.usecase.AddJavaInstallationUseCase
import app.vercors.shared.command.AbstractCommand
import app.vercors.shared.command.CommandError
import arrow.core.raise.Raise
import arrow.core.raise.context.withError
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.validate
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet

@ContributesIntoSet(AppScope::class)
class JavaAddCommand(private val addJavaInstallation: AddJavaInstallationUseCase) : AbstractCommand("java-add") {
    private val path by argument(help = "Path to a Java installation directory or executable").validate {
        require(it.isNotEmpty()) { "Path cannot be empty" }
    }

    context(_: Raise<CommandError>)
    override suspend fun execute() {
        val installation = withError({ it.toCommandError() }) { addJavaInstallation(path) }
        echo("Added Java ${installation.majorVersion} installation at ${installation.path}")
    }

    override fun help(context: Context): String = "Manually registers a Java installation"

    private fun JavaInstallationError.toCommandError(): CommandError = when (this) {
        is JavaInstallationError.NotAJavaInstallation -> CommandError("'$path' does not look like a valid Java installation")
    }
}
