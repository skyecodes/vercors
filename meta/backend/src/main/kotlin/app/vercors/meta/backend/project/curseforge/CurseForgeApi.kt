package app.vercors.meta.backend.project.curseforge

import app.vercors.meta.backend.project.curseforge.dto.CurseForgeProjectSearchResponse
import app.vercors.meta.backend.project.curseforge.dto.CurseForgeProjectSearchSortField
import app.vercors.meta.backend.project.curseforge.dto.CurseForgeSortOrder
import de.jensklingenberg.ktorfit.Ktorfit
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Query
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import org.koin.core.annotation.Property
import org.koin.core.annotation.Single


internal const val MINECRAFT_GAME_ID = 432
internal const val MODPACK_CLASS_ID = 4471
internal const val MOD_CLASS_ID = 6
internal const val RESOURCEPACK_CLASS_ID = 12
internal const val SHADERPACK_CLASS_ID = 6552
internal const val WORLD_CLASS_ID = 17

interface CurseForgeApi {
    @GET("/v1/mods/search")
    suspend fun search(
        @Query gameId: Int,
        @Query classId: Int,
        @Query sortField: CurseForgeProjectSearchSortField,
        @Query sortOrder: CurseForgeSortOrder,
        @Query pageSize: Int
    ): CurseForgeProjectSearchResponse
}

@Single
fun provideCurseForgeApi(
    httpClient: HttpClient,
    @Property("CURSEFORGE_API_KEY") curseForgeApiKey: String
): CurseForgeApi = Ktorfit.Builder()
    .httpClient(httpClient.config {
        defaultRequest {
            header("x-api-key", curseForgeApiKey)
        }
    })
    .baseUrl("https://api.curseforge.com")
    .build()
    .createCurseForgeApi()