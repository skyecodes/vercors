package app.vercors.feature.java.command

import app.vercors.feature.java.domain.usecase.ScanJavaInstallationsUseCase
import app.vercors.shared.command.AbstractCommand
import app.vercors.shared.command.CommandError
import arrow.core.raise.Raise
import com.github.ajalt.clikt.core.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet

@ContributesIntoSet(AppScope::class)
class JavaScanCommand(private val scanJavaInstallations: ScanJavaInstallationsUseCase) : AbstractCommand("java-scan") {
    context(_: Raise<CommandError>)
    override suspend fun execute() {
        echo("Scanning for Java installations...")
        scanJavaInstallations()
        echo("Done. Run 'vercors java-list' to see the results.")
    }

    override fun help(context: Context): String = "Scans the computer for Java installations"
}
