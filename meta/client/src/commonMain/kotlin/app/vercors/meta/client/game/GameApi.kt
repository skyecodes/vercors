package app.vercors.meta.client.game

import app.vercors.meta.client.ApiResponse
import de.jensklingenberg.ktorfit.http.GET
import io.ktor.utils.io.*

interface GameApi {
    @GET("game/version")
    suspend fun getVersionManifest(): ApiResponse<ByteReadChannel>

    @GET("game/java")
    suspend fun getJavaManifest(): ApiResponse<ByteReadChannel>
}
