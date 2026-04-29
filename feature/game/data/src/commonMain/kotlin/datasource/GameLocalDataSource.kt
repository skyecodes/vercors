package app.vercors.feature.game.data.datasource

import app.vercors.core.filesystem.FileStore
import app.vercors.core.filesystem.FileStoreFactory
import app.vercors.core.filesystem.writeFromSource
import app.vercors.feature.game.data.dto.MojangAssetIndex
import app.vercors.feature.game.data.dto.MojangVersionInfo
import app.vercors.feature.game.data.dto.MojangVersionManifest
import app.vercors.feature.game.domain.model.GameVersionDetails
import app.vercors.feature.game.domain.model.GameVersionList
import app.vercors.shared.data.FileValidationFailure
import app.vercors.shared.data.FileValidationRequest
import app.vercors.shared.data.FileValidationService
import arrow.core.raise.Raise
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.io.Source
import kotlinx.io.files.Path

private val logger = KotlinLogging.logger {}

@SingleIn(AppScope::class)
@Inject
class GameLocalDataSource(
    appPath: Path,
    private val fileStoreFactory: FileStoreFactory,
    private val fileValidationService: FileValidationService
) : FileStore<MojangVersionManifest> by fileStoreFactory(
    filePath = Path(appPath, "data", "versions", "manifest.json"),
    serializer = MojangVersionManifest.serializer()
) {
    private val assetIndexesPath = Path(appPath, "data", "assets", "indexes")
    private val versionsPath = Path(appPath, "data", "versions")

    context(_: Raise<FileValidationFailure>)
    suspend fun checkAndLoadVersionDetails(version: GameVersionList.Version): MojangVersionInfo =
        fileValidationService.submitAndRead(version.toValidationRequest(), MojangVersionInfo.serializer())

    suspend fun writeVersionDetails(source: Source, versionId: String) {
        val versionPath = getVersionPath(versionId)
        versionPath.writeFromSource(source)
    }

    context(_: Raise<FileValidationFailure>)
    suspend fun checkAndLoadAssetIndex(assetIndex: GameVersionDetails.AssetIndex): MojangAssetIndex =
        fileValidationService.submitAndRead(assetIndex.toValidationRequest(), MojangAssetIndex.serializer())

    suspend fun writeAssetIndex(source: Source, assetIndexId: String) {
        val assetIndexPath = getAssetIndexPath(assetIndexId)
        assetIndexPath.writeFromSource(source)
    }

    private fun getVersionPath(versionId: String) = Path(versionsPath, versionId, "${versionId}.json")

    private fun getAssetIndexPath(assetIndexId: String) = Path(assetIndexesPath, "${assetIndexId}.json")

    private fun GameVersionList.Version.toValidationRequest(): FileValidationRequest = FileValidationRequest.Url(
        path = getVersionPath(id),
        url = url,
        sha1 = sha1
    )

    private fun GameVersionDetails.AssetIndex.toValidationRequest(): FileValidationRequest = FileValidationRequest.Url(
        path = getAssetIndexPath(id),
        url = url,
        size = size,
        sha1 = sha1
    )
}
