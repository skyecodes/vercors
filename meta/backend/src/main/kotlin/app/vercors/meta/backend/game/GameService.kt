package app.vercors.meta.backend.game

import app.vercors.meta.backend.game.mojang.MojangApi
import app.vercors.meta.backend.game.mojang.dto.MojangVersionInfo
import app.vercors.meta.lib.game.GameVersionDetails
import app.vercors.meta.lib.game.GameVersionList
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.server.plugins.*
import org.koin.core.annotation.Single

@Single
class GameService(
    private val api: MojangApi,
    private val httpClient: HttpClient
) {
    suspend fun getVersionList(): GameVersionList = api.getVersionManifest().convert()

    suspend fun getVersionDetails(id: String): GameVersionDetails {
        val url = api.getVersionManifest().versions.find { it.id == id }?.url
            ?: throw NotFoundException("Version $id not found")
        return httpClient.get(url).body<MojangVersionInfo>().convert()
    }
}

