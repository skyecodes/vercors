package app.vercors.feature.java.command

import app.vercors.feature.java.domain.model.JavaInstallationError
import app.vercors.feature.java.domain.usecase.AddJavaInstallationUseCase
import app.vercors.shared.command.AbstractCommand
import app.vercors.shared.command.CommandError
import arrow.core.raise.Raise
import arrow.core.raise.context.withError
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.validate
import dev.zacsweers.metro.Inject

@Inject
class JavaAddCommand(private val addJavaInstallation: AddJavaInstallationUseCase) : AbstractCommand("add") {
    private val path by argument(help = "Path to a Java installation directory or executable").validate {
        require(it.isNotEmpty()) { "Path cannot be empty" }
    }

    context(_: Raise<CommandError>)
    override suspend fun execute() {
        val installation = withError({ CommandError("Failed to add Java installation: ${it.message}") }) {
            addJavaInstallation(path)
        }
        echo("Added Java ${installation.majorVersion} installation at ${installation.path}")
    }

    override fun help(context: Context): String = "Register a Java installation manually"

    private val JavaInstallationError.message: String
        get() = when (this) {
            is JavaInstallationError.NotAJavaInstallation -> "'$path' does not look like a valid Java installation"
        }
}
