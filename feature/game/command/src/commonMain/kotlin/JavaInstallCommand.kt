package app.vercors.feature.game.command

import app.vercors.feature.game.domain.usecase.InstallJavaRuntimeUseCase
import app.vercors.shared.command.AbstractCommand
import app.vercors.shared.command.CommandError
import arrow.core.raise.Raise
import arrow.core.raise.context.withError
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.int
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet

@ContributesIntoSet(AppScope::class)
class JavaInstallCommand(private val installJavaRuntime: InstallJavaRuntimeUseCase) : AbstractCommand("java-install") {
    private val majorVersion by argument(help = "Java major version to install, e.g. 21").int()

    context(_: Raise<CommandError>)
    override suspend fun execute() {
        echo("Installing Java $majorVersion...")
        withError({ CommandError("Failed to install Java $majorVersion: $it") }) { installJavaRuntime(majorVersion) }
        echo("Java $majorVersion installed.")
    }

    override fun help(context: Context): String = "Downloads and installs a Java runtime from Mojang"
}
