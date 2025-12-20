package app.vercors.meta.backend.game

import app.vercors.meta.backend.game.mojang.MojangApi
import app.vercors.meta.backend.game.mojang.dto.MojangVersionInfo
import app.vercors.meta.lib.game.GameVersionDetailsData
import app.vercors.meta.lib.game.GameVersionListData
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.server.plugins.*
import org.koin.core.annotation.Single

@Single
class GameService(
    private val api: MojangApi,
    private val httpClient: HttpClient,
) {
    private var urlCache: Map<String, String>? = null

    suspend fun getVersionList(): GameVersionListData {
        val manifest = api.getVersionManifest()
        urlCache = manifest.versions.associate { it.id to it.url }
        return manifest.convert()
    }

    suspend fun getVersionDetails(id: String): GameVersionDetailsData {
        if (urlCache == null) getVersionList()
        val url = urlCache!![id] ?: throw NotFoundException("Version $id not found")
        return httpClient.get(url).body<MojangVersionInfo>().convert()
    }
}
