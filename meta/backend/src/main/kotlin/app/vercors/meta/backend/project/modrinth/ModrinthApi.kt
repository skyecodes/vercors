package app.vercors.meta.backend.project.modrinth

import app.vercors.meta.backend.project.modrinth.dto.ModrinthProject
import app.vercors.meta.backend.project.modrinth.dto.ModrinthProjectMember
import app.vercors.meta.backend.project.modrinth.dto.ModrinthProjectSearchResult
import de.jensklingenberg.ktorfit.Ktorfit
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query
import io.ktor.client.*
import org.koin.core.annotation.Single

interface ModrinthApi {
    @GET("v2/search")
    suspend fun search(
        @Query("query") query: String? = null,
        @Query("facets") facets: String? = null,
        @Query("index") index: String? = null,
        @Query("offset") offset: Int? = null,
        @Query("limit") limit: Int? = null,
    ): ModrinthProjectSearchResult

    @GET("v2/project/{projectId}")
    suspend fun getProject(@Path("projectId") projectId: String): ModrinthProject

    @GET("v2/project/{projectId}/members")
    suspend fun getProjectMembers(@Path("projectId") projectId: String): List<ModrinthProjectMember>
}

@Single
fun provideModrinthApi(
    httpClient: HttpClient
): ModrinthApi = Ktorfit.Builder()
    .httpClient(httpClient)
    .baseUrl("https://api.modrinth.com/")
    .build()
    .createModrinthApi()