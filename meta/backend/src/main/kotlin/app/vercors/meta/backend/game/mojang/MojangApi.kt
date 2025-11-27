package app.vercors.meta.backend.game.mojang

import app.vercors.meta.backend.game.mojang.dto.MojangVersionManifest
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import org.koin.core.annotation.Single

@Single
class MojangApi(private val httpClient: HttpClient) {
    suspend fun getVersionManifest(): MojangVersionManifest =
        httpClient.get("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json").body()
}