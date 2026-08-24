package app.vercors.feature.launch.command

import app.vercors.feature.game.domain.model.GameVersionError
import app.vercors.feature.launcher.domain.model.InstanceLaunchError
import app.vercors.feature.launcher.domain.usecase.LaunchInstanceUseCase
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
class LaunchCommand(private val launchInstance: LaunchInstanceUseCase) : AbstractCommand("launch") {
    private val id by argument(help = "Instance ID").validate {
        require(it.isNotEmpty()) { "Instance ID cannot be empty" }
    }

    context(_: Raise<CommandError>)
    override suspend fun execute() {
        withError({ CommandError("Failed to launch instance: ${it.message}") }) { launchInstance(id) }
    }

    override fun help(context: Context): String = "Launch an instance"

    private val InstanceLaunchError.message: String
        get() = when (this) {
            is InstanceLaunchError.InstanceNotFound -> "Instance with ID $id not found"
            is InstanceLaunchError.AccountLoading -> "Account loading error"
            is InstanceLaunchError.GameVersion -> "Game version error: ${
                when (val e = cause) {
                    is GameVersionError.InvalidAssetIndex -> "Invalid asset index: $e"
                    is GameVersionError.InvalidDetails -> "Invalid version details: $e"
                    is GameVersionError.InvalidManifest -> "Invalid manifest: $e"
                    is GameVersionError.NotFound -> "Version ${e.versionId} not found"
                }
            }"
            is InstanceLaunchError.ModLoaderInstall -> "Mod loader install error: $cause"
            is InstanceLaunchError.NoCompatibleJava ->
                "No Java $majorVersion installation found. Run 'vercors java-install $majorVersion' or add one via the Settings screen."
            InstanceLaunchError.LoginRequired -> "No account selected. Log in via the app, then retry."
        }
}
