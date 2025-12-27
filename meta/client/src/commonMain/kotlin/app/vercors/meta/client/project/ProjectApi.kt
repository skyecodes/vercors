package app.vercors.meta.client.project

import app.vercors.meta.lib.ApiResponse
import app.vercors.meta.lib.project.*
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query

interface ProjectApi {
    @GET("platform")
    suspend fun getPlatformList(): ApiResponse<List<ProjectPlatformData>>

    @GET("platform/{platformId}")
    suspend fun getPlatformDetails(
        @Path("platformId") platformId: String
    ): ApiResponse<ProjectPlatformData>

    @GET("platform/{platformId}/featured-project")
    suspend fun getFeaturedProjectList(
        @Path("platformId") platformId: String
    ): ApiResponse<Map<ProjectTypeData, List<BriefProjectData>>>

    @GET("platform/{platformId}/project")
    suspend fun getProjectList(
        @Path("platformId") platformId: String,
        @Query("type") projectType: String,
        @Query("loaders") loaders: List<String>?,
        @Query("gameVersions") gameVersions: List<String>?,
        @Query("categories") categories: List<String>?,
        @Query("query") query: String?,
        @Query("sortBy") sortBy: String?,
        @Query("pageSize") pageSize: Int?,
        @Query("offset") offset: Int?,
    ): ApiResponse<ProjectSearchResponseData>

    @GET("platform/{platformId}/project/{projectId}")
    suspend fun getProject(
        @Path("platformId") platformId: String,
        @Path("projectId") projectId: String
    ): ApiResponse<ProjectData>
}
