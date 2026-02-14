package app.vercors.feature.project.backend.modrinth

import app.vercors.feature.project.backend.PlatformResponse
import app.vercors.feature.project.backend.modrinth.dto.ModrinthCategory
import app.vercors.feature.project.backend.modrinth.dto.ModrinthProject
import app.vercors.feature.project.backend.modrinth.dto.ModrinthProjectMember
import app.vercors.feature.project.backend.modrinth.dto.ModrinthProjectSearchResult
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query

interface ModrinthApi {
    @GET("v2/tag/project_type")
    suspend fun getProjectTypes(): PlatformResponse<List<String>>

    @GET("v2/tag/category")
    suspend fun getCategories(): PlatformResponse<List<ModrinthCategory>>

    @GET("v2/search")
    suspend fun search(
        @Query("query") query: String? = null,
        @Query("facets") facets: String? = null,
        @Query("index") index: String? = null,
        @Query("offset") offset: Int? = null,
        @Query("limit") limit: Int? = null,
    ): PlatformResponse<ModrinthProjectSearchResult>

    @GET("v2/project/{projectId}")
    suspend fun getProject(@Path("projectId") projectId: String): PlatformResponse<ModrinthProject>

    @GET("v2/project/{projectId}/members")
    suspend fun getProjectMembers(@Path("projectId") projectId: String): PlatformResponse<List<ModrinthProjectMember>>
}
