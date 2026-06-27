package app.vercors.feature.game.data.repository

import app.vercors.core.domain.MutableProgressiveStateFlow
import app.vercors.feature.game.api.Game
import app.vercors.feature.game.data.datasource.GameLocalDataSource
import app.vercors.feature.game.data.dto.MojangVersionManifest
import app.vercors.feature.game.data.toDomain
import app.vercors.feature.game.domain.model.GameAssetIndex
import app.vercors.feature.game.domain.model.GameVersionDetails
import app.vercors.feature.game.domain.model.GameVersionError
import app.vercors.feature.game.domain.model.GameVersionError.InvalidAssetIndex
import app.vercors.feature.game.domain.model.GameVersionError.InvalidDetails
import app.vercors.feature.game.domain.model.GameVersionList
import app.vercors.feature.game.domain.repository.GameRepository
import app.vercors.shared.api.ApiV1
import app.vercors.shared.data.FileValidationRequest
import app.vercors.shared.data.FileValidationService
import arrow.core.raise.Raise
import arrow.core.raise.context.bind
import arrow.core.raise.context.either
import arrow.core.raise.context.ensureNotNull
import arrow.core.raise.context.withError
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.files.Path
import opensavvy.progress.loading
import opensavvy.spine.api.div
import opensavvy.spine.client.arrow.body
import opensavvy.spine.client.request
import opensavvy.state.ExperimentalProgressiveRaiseApi
import opensavvy.state.arrow.toEither
import opensavvy.state.coroutines.ProgressiveFlow
import opensavvy.state.coroutines.now
import opensavvy.state.progressive.copy
import opensavvy.state.progressive.failedWithProgress
import opensavvy.state.progressive.successfulWithProgress

private val logger = KotlinLogging.logger {}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class GameRepositoryImpl(
    private val fileValidationService: FileValidationService,
    private val gameLocalDataSource: GameLocalDataSource,
    appPath: Path,
) : GameRepository {
    private val dataPath = Path(appPath, "data")
    private val versionsPath = Path(dataPath, "versions")
    private val assetIndexesPath = Path(dataPath, "assets", "indexes")
    private val versionManifestFileValidationRequest = FileValidationRequest.Channel(
        channel = { it.request(ApiV1 / Game / Game.versions).body() },
        path = Path(versionsPath, "manifest.json"),
        alwaysRedownload = true,
    )
    private val versionManifestMutex = Mutex()
    private val versionManifestState: MutableProgressiveStateFlow<GameVersionError.InvalidManifest, GameVersionList> =
        MutableProgressiveStateFlow()

    @OptIn(ExperimentalProgressiveRaiseApi::class)
    override fun observeVersionManifest(): ProgressiveFlow<GameVersionError.InvalidManifest, GameVersionList> =
        versionManifestState.onStart { syncVersionManifest() }

    private suspend fun syncVersionManifest() {
        if (versionManifestMutex.isLocked) return
        versionManifestMutex.withLock {
            logger.debug { "Syncing version manifest..." }
            versionManifestState.update { it.copy(loading()) }
            val result = either {
                fileValidationService.submitAndRead(
                    versionManifestFileValidationRequest,
                    MojangVersionManifest.serializer()
                )
            }.fold(
                ifLeft = { error ->
                    GameVersionError.InvalidManifest(error).failedWithProgress().also {
                        logger.debug { "Version manifest sync failed: ${it.failure}" }
                    }
                },
                ifRight = { manifest ->
                    manifest.toDomain().successfulWithProgress().also {
                        logger.debug { "Version manifest synced successfully" }
                    }
                }
            )
            versionManifestState.emit(result)
        }
    }

    context(_: Raise<GameVersionError>)
    override suspend fun getVersionDetails(versionId: String): GameVersionDetails {
        val manifest = observeVersionManifest().now().toEither().bind()
        val version =
            ensureNotNull(manifest.versions.find { it.id == versionId }) { GameVersionError.NotFound(versionId) }
        return withError(::InvalidDetails) { gameLocalDataSource.checkAndLoadVersionDetails(version).toDomain() }
    }

    context(_: Raise<InvalidAssetIndex>)
    override suspend fun getAssetIndex(assetIndex: GameVersionDetails.AssetIndex): GameAssetIndex =
        withError(::InvalidAssetIndex) { gameLocalDataSource.checkAndLoadAssetIndex(assetIndex).toDomain() }
}
