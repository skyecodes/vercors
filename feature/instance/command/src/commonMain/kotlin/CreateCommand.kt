package app.vercors.feature.instance.command

import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.game.domain.usecase.ObserveGameVersionListUseCase
import app.vercors.feature.instance.domain.model.Instance
import app.vercors.feature.instance.domain.model.InstanceModLoader
import app.vercors.feature.instance.domain.model.InstanceValidationError
import app.vercors.feature.instance.domain.usecase.CreateInstanceUseCase
import app.vercors.shared.command.AbstractCommand
import app.vercors.shared.command.CommandError
import arrow.core.raise.Raise
import arrow.core.raise.context.bind
import arrow.core.raise.context.withError
import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.ajalt.clikt.parameters.options.validate
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import opensavvy.state.arrow.toEither
import opensavvy.state.coroutines.now

internal const val LATEST_RELEASE = "latest-release"
internal const val LATEST_SNAPSHOT = "latest-snapshot"

@ContributesIntoSet(AppScope::class)
class CreateCommand(
    private val observeGameVersionList: ObserveGameVersionListUseCase,
    private val createInstance: CreateInstanceUseCase
) : AbstractCommand("create") {
    private val gameVersion by option(help = "Minecraft version")
        .default(LATEST_RELEASE)
        .validate {
            require(it.isNotEmpty()) { "Minecraft version cannot be empty" }
        }
    private val loader by option(help = "Mod loader")
        .default("")
        .validate { value ->
            require(value.isEmpty() || value in ModLoaderType.entries.map { it.name.lowercase() }) { "Invalid mod loader" }
        }
    private val name by option(help = "Instance name")
        .prompt(text = "Instance name")
        .validate {
            require(it.length >= 3) { "Instance name must be at least 3 characters long" }
        }

    context(_: Raise<CommandError>)
    override suspend fun execute() {
        val resolvedGameVersion = resolveGameVersion()
        val instance = Instance(
            name = name,
            gameVersion = resolvedGameVersion,
            modLoader = parseLoader(loader)?.let { InstanceModLoader(type = it, version = LATEST_RELEASE) }
        )
        val createdInstance =
            withError({ CommandError("Could not create instance: ${it.message}") }) { createInstance(instance) }
        echo("Instance created with ID: ${createdInstance.id}")
    }

    override fun help(context: Context): String = "Create a new instance"

    context(_: Raise<CommandError>)
    private suspend fun resolveGameVersion(): String {
        val versionList = withError({ CommandError("Could not retrieve game version list") }) {
            observeGameVersionList().now().toEither().bind()
        }
        val version = when (gameVersion) {
            LATEST_RELEASE -> versionList.latestRelease
            LATEST_SNAPSHOT -> versionList.latestSnapshot
            else -> gameVersion
        }
        if (versionList.versions.none { it.id == version }) {
            throw BadParameterValue("Minecraft version '$version' not found")
        }
        return version
    }

    private fun parseLoader(loader: String): ModLoaderType? =
        ModLoaderType.entries.find { it.name.equals(loader, ignoreCase = true) }

    private val InstanceValidationError.message: String
        get() = when (this) {
            is InstanceValidationError.NameTooShort -> "Instance name must be at least 3 characters long"
            is InstanceValidationError.EmptyInstanceId -> "Instance ID cannot be empty"
        }
}
