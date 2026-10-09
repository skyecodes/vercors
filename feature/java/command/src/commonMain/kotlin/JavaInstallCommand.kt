package app.vercors.feature.java.command

import app.vercors.feature.java.domain.model.JavaRuntimeError
import app.vercors.feature.java.domain.usecase.InstallJavaRuntimeUseCase
import app.vercors.shared.command.AbstractCommand
import app.vercors.shared.command.CommandError
import arrow.core.raise.Raise
import arrow.core.raise.context.withError
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.int
import dev.zacsweers.metro.Inject

@Inject
class JavaInstallCommand(private val installJavaRuntime: InstallJavaRuntimeUseCase) : AbstractCommand("install") {
    private val majorVersion by argument(help = "Java major version to install, e.g. 21").int()

    context(_: Raise<CommandError>)
    override suspend fun execute() {
        echo("Installing Java $majorVersion...")
        withError({ CommandError("Failed to install Java $majorVersion: ${it.message}") }) {
            installJavaRuntime(majorVersion)
        }
        echo("Java $majorVersion installed.")
    }

    override fun help(context: Context): String = "Download and install a Java runtime from Mojang"

    private val JavaRuntimeError.message: String
        get() = when (this) {
            is JavaRuntimeError.InvalidManifest -> "Invalid Java runtime manifest: $cause"
            is JavaRuntimeError.ComponentNotAvailable -> "Java $majorVersion is not available for this platform"
            is JavaRuntimeError.DownloadFailed -> "Download failed: $cause"
            JavaRuntimeError.ProbeFailed -> "The installed runtime could not be verified"
        }
}
