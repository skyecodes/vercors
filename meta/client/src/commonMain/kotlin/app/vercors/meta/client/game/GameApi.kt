package app.vercors.meta.client.game

import app.vercors.meta.lib.ApiResponse
import app.vercors.meta.lib.game.GameVersionDetails
import app.vercors.meta.lib.game.GameVersionList
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path

interface GameApi {
    @GET("v1/game/versions")
    suspend fun getGameVersionList(): ApiResponse<GameVersionList>

    @GET("v1/game/versions/{id}")
    suspend fun getGameVersionDetails(@Path id: String): ApiResponse<GameVersionDetails>
}