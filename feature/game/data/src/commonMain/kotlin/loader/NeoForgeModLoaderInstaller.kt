package app.vercors.feature.game.data.loader

import app.vercors.core.system.ProcessRunner
import app.vercors.core.system.SystemService
import app.vercors.feature.game.domain.model.GameVersionDetails
import app.vercors.feature.game.domain.model.LoaderInstallProfile
import app.vercors.feature.game.domain.model.ModLoaderInstallError
import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.game.domain.service.ModLoaderInstaller
import app.vercors.shared.data.AppPaths
import app.vercors.shared.data.FileValidationService
import arrow.core.raise.Raise
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.StringKey
import kotlinx.serialization.json.Json

@SingleIn(AppScope::class)
@ContributesIntoMap(AppScope::class)
@StringKey("neoforge")
class NeoForgeModLoaderInstaller(
    private val appPaths: AppPaths,
    private val fileValidationService: FileValidationService,
    private val processRunner: ProcessRunner,
    private val systemService: SystemService,
    private val json: Json
) : ModLoaderInstaller {
    context(_: Raise<ModLoaderInstallError>)
    override suspend fun resolve(gameVersion: GameVersionDetails, loaderVersion: String): LoaderInstallProfile =
        installForgeFamily(
            loaderType = ModLoaderType.NeoForge,
            loaderKey = "neoforge",
            gameVersion = gameVersion,
            loaderVersion = loaderVersion,
            appPaths = appPaths,
            fileValidationService = fileValidationService,
            processRunner = processRunner,
            systemService = systemService,
            json = json
        )
}
