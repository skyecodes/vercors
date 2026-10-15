package app.vercors.feature.game.data.loader

import app.vercors.feature.game.data.dto.FabricLoaderProfile
import app.vercors.feature.game.data.toDomain
import app.vercors.feature.game.domain.model.GameVersionDetails
import app.vercors.feature.game.domain.model.LoaderInstallProfile
import app.vercors.feature.game.domain.model.ModLoaderInstallError
import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.game.domain.service.key
import app.vercors.shared.data.AppPaths
import app.vercors.shared.data.FileValidationService
import arrow.core.raise.Raise
import arrow.core.raise.context.withError
import dev.zacsweers.metro.Inject

@Inject
class FabricFamilyInstaller(
    private val appPaths: AppPaths,
    private val fileValidationService: FileValidationService
) {
    context(_: Raise<ModLoaderInstallError>)
    suspend fun install(
        loaderType: ModLoaderType,
        gameVersion: GameVersionDetails,
        loaderVersion: String
    ): LoaderInstallProfile = withError({ ModLoaderInstallError.FetchFailed(loaderType, it) }) {
        val request = loaderInstallArtifactRequest(
            gameVersionId = gameVersion.id,
            loaderId = loaderType.key,
            loaderVersion = loaderVersion,
            path = appPaths.loaderProfileFile(gameVersion.id, loaderType.key, loaderVersion)
        )
        fileValidationService.submitAndRead(request, FabricLoaderProfile.serializer()).toDomain()
    }
}
