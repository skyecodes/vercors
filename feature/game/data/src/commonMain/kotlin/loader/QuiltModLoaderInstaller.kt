package app.vercors.feature.game.data.loader

import app.vercors.feature.game.data.dto.FabricLoaderProfile
import app.vercors.feature.game.data.toDomain
import app.vercors.feature.game.domain.model.GameVersionDetails
import app.vercors.feature.game.domain.model.LoaderInstallProfile
import app.vercors.feature.game.domain.model.ModLoaderInstallError
import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.game.domain.service.ModLoaderInstaller
import app.vercors.shared.data.AppPaths
import app.vercors.shared.data.FileValidationRequest
import app.vercors.shared.data.FileValidationService
import arrow.core.raise.Raise
import arrow.core.raise.context.withError
import dev.zacsweers.metro.*

/** Fetches Quilt's `profile/json`, which is already everything needed to launch: no local processing. */
@SingleIn(AppScope::class)
@ContributesIntoMap(AppScope::class)
@StringKey("quilt")
@Inject
class QuiltModLoaderInstaller(
    private val appPaths: AppPaths,
    private val fileValidationService: FileValidationService
) : ModLoaderInstaller {
    context(_: Raise<ModLoaderInstallError>)
    override suspend fun resolve(gameVersion: GameVersionDetails, loaderVersion: String): LoaderInstallProfile =
        withError({ ModLoaderInstallError.FetchFailed(ModLoaderType.Quilt, it) }) {
            val request = FileValidationRequest.Url(
                url = "https://meta.quiltmc.org/v3/versions/loader/${gameVersion.id}/$loaderVersion/profile/json",
                path = appPaths.loaderProfileFile(gameVersion.id, "quilt", loaderVersion)
            )
            fileValidationService.submitAndRead(request, FabricLoaderProfile.serializer()).toDomain()
        }
}
