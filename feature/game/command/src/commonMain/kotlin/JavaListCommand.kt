package app.vercors.feature.game.command

import app.vercors.feature.game.domain.usecase.ObserveJavaInstallationsUseCase
import app.vercors.shared.command.AbstractCommand
import app.vercors.shared.command.CommandError
import app.vercors.shared.command.plural
import arrow.core.raise.Raise
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.mordant.rendering.BorderType
import com.github.ajalt.mordant.table.table
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import opensavvy.state.coroutines.now
import opensavvy.state.outcome.value

@ContributesIntoSet(AppScope::class)
class JavaListCommand(private val observeJavaInstallations: ObserveJavaInstallationsUseCase) :
    AbstractCommand("java-list") {
    context(_: Raise<CommandError>)
    override suspend fun execute() {
        val installations = observeJavaInstallations().now().value
        if (installations.isEmpty()) {
            echo("No Java installation found. Run 'vercors java-scan' or 'vercors java-add <path>'.")
        } else {
            echo("Found ${installations.size} Java installation${plural(installations)}:")
            echo(table {
                borderType = BorderType.SQUARE_DOUBLE_SECTION_SEPARATOR
                header { row("Version", "Vendor", "Arch", "Path", "Manual") }
                body {
                    installations.forEach {
                        row(it.versionString, it.vendor, it.arch, it.path, if (it.isManuallyAdded) "yes" else "no")
                    }
                }
            })
        }
    }

    override fun help(context: Context): String = "Lists detected Java installations"
}
