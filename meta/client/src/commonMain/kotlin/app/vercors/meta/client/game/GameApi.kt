package app.vercors.meta.client.game

import app.vercors.meta.lib.ApiResponse
import app.vercors.meta.lib.game.GameVersionDetailsData
import app.vercors.meta.lib.game.GameVersionListData
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path

interface GameApi {
    @GET("game/version")
    suspend fun getGameVersionList(): ApiResponse<GameVersionListData>

    @GET("game/version/{id}")
    suspend fun getGameVersionDetails(@Path id: String): ApiResponse<GameVersionDetailsData>
}
