package app.vercors.meta.client.project

import app.vercors.meta.lib.ApiResponse
import app.vercors.meta.lib.project.PlatformData
import app.vercors.meta.lib.project.ProjectData
import app.vercors.meta.lib.project.ProjectTypeData
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path

interface ProjectApi {
    @GET("project/type")
    suspend fun getProjectTypeList(): ApiResponse<List<ProjectTypeData>>

    @GET("project/platform")
    suspend fun getPlatformList(): ApiResponse<List<PlatformData>>

    @GET("project/platform/{platformId}")
    suspend fun getPlatformDetails(@Path("platformId") platformId: String): ApiResponse<PlatformData>

    @GET("project/platform/{platformId}/featured")
    suspend fun getFeaturedProjectList(@Path("platformId") platformId: String): ApiResponse<Map<ProjectTypeData, List<ProjectData>>>
}