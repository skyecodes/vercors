package app.vercors.meta.client.project

import app.vercors.meta.lib.ApiResponse
import app.vercors.meta.lib.project.BriefProjectData
import app.vercors.meta.lib.project.PlatformData
import app.vercors.meta.lib.project.ProjectData
import app.vercors.meta.lib.project.ProjectTypeData
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path

interface ProjectApi {
    @GET("platform")
    suspend fun getPlatformList(): ApiResponse<List<PlatformData>>

    @GET("platform/{platformId}")
    suspend fun getPlatformDetails(
        @Path("platformId") platformId: String
    ): ApiResponse<PlatformData>

    @GET("platform/{platformId}/featured-project")
    suspend fun getFeaturedProjectList(
        @Path("platformId") platformId: String
    ): ApiResponse<Map<ProjectTypeData, List<BriefProjectData>>>

    @GET("platform/{platformId}/project/{projectId}")
    suspend fun getProject(
        @Path("platformId") platformId: String,
        @Path("projectId") projectId: String
    ): ApiResponse<ProjectData>
}
