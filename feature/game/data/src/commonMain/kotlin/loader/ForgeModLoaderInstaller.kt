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

/**
 * Downloads Forge's installer and runs its processors (binary-patching the client jar, splitting/renaming it,
 * etc.) to produce the same `mainClass`/`libraries` shape Fabric/Quilt already return more directly.
 */
@SingleIn(AppScope::class)
@ContributesIntoMap(AppScope::class)
@StringKey("forge")
@Inject
class ForgeModLoaderInstaller(
    private val appPaths: AppPaths,
    private val fileValidationService: FileValidationService,
    private val processRunner: ProcessRunner,
    private val systemService: SystemService,
    private val json: Json
) : ModLoaderInstaller {
    context(_: Raise<ModLoaderInstallError>)
    override suspend fun resolve(gameVersion: GameVersionDetails, loaderVersion: String): LoaderInstallProfile =
        installForgeFamily(
            loaderType = ModLoaderType.Forge,
            loaderKey = "forge",
            gameVersion = gameVersion,
            loaderVersion = loaderVersion,
            appPaths = appPaths,
            fileValidationService = fileValidationService,
            processRunner = processRunner,
            systemService = systemService,
            json = json
        )
}
