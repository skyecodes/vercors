package app.vercors.feature.java.command

import app.vercors.shared.command.AbstractCommand
import app.vercors.shared.command.CommandError
import arrow.core.raise.Raise
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.subcommands
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet

@ContributesIntoSet(AppScope::class)
class JavaCommand(
    addCommand: JavaAddCommand,
    installCommand: JavaInstallCommand,
    listCommand: JavaListCommand,
    scanCommand: JavaScanCommand
) : AbstractCommand("java") {
    override val printHelpOnEmptyArgs = true

    init {
        subcommands(addCommand, installCommand, listCommand, scanCommand)
    }

    context(_: Raise<CommandError>)
    override suspend fun execute() = Unit

    override fun help(context: Context): String = "Manage Java installations"
}
