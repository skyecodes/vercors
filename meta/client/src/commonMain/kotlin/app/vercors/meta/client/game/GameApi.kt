package app.vercors.meta.client.game

import de.jensklingenberg.ktorfit.Ktorfit
import de.jensklingenberg.ktorfit.http.GET
import io.ktor.utils.io.*
import org.koin.core.annotation.Single

interface GameApi {
    @GET("game/version")
    suspend fun getVersionManifest(): ByteReadChannel

    @GET("game/java")
    suspend fun getJavaManifest(): ByteReadChannel
}

@Single
internal fun provideGameApi(ktorfit: Ktorfit): GameApi = ktorfit.createGameApi()
