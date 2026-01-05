package app.vercors.meta.client.game

import de.jensklingenberg.ktorfit.http.GET
import io.ktor.utils.io.*

interface GameApi {
    @GET("game/version")
    suspend fun getVersionManifest(): ByteReadChannel

    @GET("game/java")
    suspend fun getJavaManifest(): ByteReadChannel
}
