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
import dev.zacsweers.metro.*
import kotlinx.serialization.json.Json

/** NeoForge is a Forge fork sharing the same installer/`install_profile.json` lineage — see [ForgeModLoaderInstaller]. */
@SingleIn(AppScope::class)
@ContributesIntoMap(AppScope::class)
@StringKey("neoforge")
@Inject
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
