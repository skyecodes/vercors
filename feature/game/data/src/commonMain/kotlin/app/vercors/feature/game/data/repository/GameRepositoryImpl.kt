package app.vercors.feature.game.data.repository

import app.vercors.core.domain.ResourceFlow
import app.vercors.core.domain.await
import app.vercors.core.domain.map
import app.vercors.core.domain.orThrow
import app.vercors.feature.game.data.datasource.GameLocalDataSource
import app.vercors.feature.game.data.toDomain
import app.vercors.feature.game.domain.model.GameAssetIndex
import app.vercors.feature.game.domain.model.GameVersionDetails
import app.vercors.feature.game.domain.model.GameVersionList
import app.vercors.feature.game.domain.repository.GameRepository
import app.vercors.meta.client.game.GameApi
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.utils.io.*
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.buffered

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class GameRepositoryImpl(
    val api: GameApi,
    val httpClient: HttpClient,
    val local: GameLocalDataSource
) : GameRepository {
    private val mutex = Mutex()

    override fun observeVersionManifest(): ResourceFlow<GameVersionList> {
        return local.manifestData
            .map { res -> res.map { it.toDomain() } }
            .onStart { syncVersionManifest() }
    }

    private suspend fun syncVersionManifest() {
        if (mutex.isLocked) return
        mutex.withLock {
            api.getVersionManifest().asSource().buffered().use { local.writeVersionManifest(it) }
        }
    }

    override suspend fun getVersionDetails(versionId: String): GameVersionDetails {
        val manifest = observeVersionManifest().await().orThrow()
        val version = requireNotNull(manifest.versions.find { it.id == versionId }) { "Unknown version $versionId" }
        return syncVersionDetails(version)
    }

    private suspend fun syncVersionDetails(version: GameVersionList.Version): GameVersionDetails {
        var localVersion = local.checkAndLoadVersionDetails(version)
        var attempts = 0
        while (attempts++ < 3 && localVersion == null) {
            httpClient.get(version.url).bodyAsChannel().asSource().buffered().use {
                local.writeVersionDetails(it, version.id)
            }
            localVersion = local.checkAndLoadVersionDetails(version)
        }
        checkNotNull(localVersion) { "Failed to download version details for ${version.id}" }
        return localVersion.toDomain()
    }

    override suspend fun getAssetIndex(assetIndex: GameVersionDetails.AssetIndex): GameAssetIndex {
        var localAssetIndex = local.checkAndLoadAssetIndex(assetIndex)
        var attempts = 0
        while (attempts++ < 3 && localAssetIndex == null) {
            httpClient.get(assetIndex.url).bodyAsChannel().asSource().buffered().use {
                local.writeAssetIndex(it, assetIndex.id)
            }
            localAssetIndex = local.checkAndLoadAssetIndex(assetIndex)
        }
        checkNotNull(localAssetIndex) { "Failed to download asset index for ${assetIndex.id}" }
        return localAssetIndex.toDomain()
    }
}
