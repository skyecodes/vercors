package app.vercors.feature.game.data.repository

import app.vercors.core.domain.ResourceFlow
import app.vercors.core.domain.map
import app.vercors.feature.game.data.datasource.GameLocalDataSource
import app.vercors.feature.game.data.toDomain
import app.vercors.feature.game.domain.model.GameVersionList
import app.vercors.feature.game.domain.repository.GameRepository
import app.vercors.meta.client.game.GameApi
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
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
}
