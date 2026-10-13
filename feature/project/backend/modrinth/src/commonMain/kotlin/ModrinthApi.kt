package app.vercors.feature.project.backend.modrinth

import app.vercors.feature.project.backend.PlatformResponse
import app.vercors.feature.project.backend.modrinth.dto.*
import de.jensklingenberg.ktorfit.http.*

interface ModrinthApi {
    @GET("v2/tag/project_type")
    suspend fun getProjectTypes(): PlatformResponse<List<ModrinthProjectType>>

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

    @GET("v2/project/{projectId}/version")
    suspend fun getVersions(
        @Path("projectId") projectId: String,
        @Query("loaders") loaders: String? = null,
        @Query("game_versions") gameVersions: String? = null
    ): PlatformResponse<List<ModrinthVersion>>

    @GET("v2/version/{id}")
    suspend fun getVersion(@Path("id") id: String): PlatformResponse<ModrinthVersion>

    @GET("v2/versions")
    suspend fun getVersionsByIds(@Query("ids") ids: String): PlatformResponse<List<ModrinthVersion>>

    @Headers("Content-Type: application/json")
    @POST("v2/version_files")
    suspend fun getVersionsByHashes(
        @Body request: ModrinthVersionFilesRequest
    ): PlatformResponse<Map<String, ModrinthVersion>>
}
