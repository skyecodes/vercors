package app.vercors.launcher.cli

import app.vercors.launcher.common.domain.game.usecase.GetGameVersionListUseCase
import app.vercors.launcher.common.domain.instance.model.Instance
import app.vercors.launcher.common.domain.instance.model.InstanceModLoader
import app.vercors.launcher.common.domain.instance.usecase.CreateInstanceUseCase
import app.vercors.launcher.common.domain.loader.ModLoaderType
import app.vercors.shared.domain.getOrNull
import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.ajalt.clikt.parameters.options.validate

private const val LATEST_RELEASE = "latest-release"
private const val LATEST_SNAPSHOT = "latest-release"

class CreateCommand : VercorsCommandBase("create") {
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

    override suspend fun execute() {
        val resolvedGameVersion = resolveGameVersion()
        val instance = createInstance(resolvedGameVersion)
        echo("Instance created with ID: ${instance.id}")
    }

    override fun help(context: Context): String = "Create a new instance"

    private suspend fun resolveGameVersion(): String {
        val getGameVersionList = koin.get<GetGameVersionListUseCase>()
        val versionList = getGameVersionList().getOrNull() ?: run {
            throw CliktError("Failed to fetch game version list")
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

    private suspend fun createInstance(resolvedGameVersion: String): Instance {
        val createInstance = koin.get<CreateInstanceUseCase>()
        val instance = Instance(
            name = name,
            gameVersion = resolvedGameVersion,
            modLoader = parseLoader(loader)?.let { InstanceModLoader(type = it, version = LATEST_RELEASE) }
        )
        createInstance(instance)
        return instance
    }

    private fun parseLoader(loader: String): ModLoaderType? =
        ModLoaderType.entries.find { it.name.equals(loader, ignoreCase = true) }
}