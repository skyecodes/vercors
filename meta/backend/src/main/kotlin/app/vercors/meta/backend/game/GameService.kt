package app.vercors.meta.backend.game

import app.vercors.meta.backend.game.mojang.MojangApi
import app.vercors.meta.backend.game.mojang.dto.MojangVersionInfo
import app.vercors.meta.lib.game.GameVersionDetailsData
import app.vercors.meta.lib.game.GameVersionListData
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@Single
@OptIn(ExperimentalAtomicApi::class)
class GameService(
    private val api: MojangApi,
    private val httpClient: HttpClient,
    private val backgroundScope: CoroutineScope
) {
    private var urlCache: AtomicReference<Map<String, String>?> = AtomicReference(null)

    suspend fun getVersionList(): GameVersionListData {
        return api.getVersionManifest().also { manifest ->
            backgroundScope.launch {
                urlCache.exchange(manifest.versions.associate { it.id to it.url })
            }
        }.convert()
    }

    suspend fun getVersionDetails(id: String): GameVersionDetailsData {
        val url = urlCache.load()?.get(id) ?: error("Version $id not found")
        return httpClient.get(url).body<MojangVersionInfo>().convert()
    }
}

