package app.vercors.meta.backend.project.curseforge

import app.vercors.meta.backend.project.curseforge.dto.*
import de.jensklingenberg.ktorfit.Ktorfit
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import org.koin.core.annotation.Property
import org.koin.core.annotation.Single

interface CurseForgeApi {
    @GET("v1/mods/search")
    suspend fun search(
        @Query("gameId") gameId: Int,
        @Query("classId") classId: Int? = null,
        @Query("pageSize") pageSize: Int? = null,
        @Query("searchFilter") searchFilter: String? = null,
        @Query("sortField") sortField: CurseForgeProjectSearchSortField? = CurseForgeProjectSearchSortField.Featured,
        @Query("sortOrder") sortOrder: CurseForgeSortOrder? = CurseForgeSortOrder.Desc
    ): CurseForgeProjectSearchResponse

    @GET("v1/mods/{projectId}")
    suspend fun getProject(@Path("projectId") projectId: String): CurseForgeResponse<CurseForgeProject>
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
    .baseUrl("https://api.curseforge.com/")
    .build()
    .createCurseForgeApi()
