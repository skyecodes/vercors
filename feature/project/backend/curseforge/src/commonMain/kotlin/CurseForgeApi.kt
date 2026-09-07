package app.vercors.feature.project.backend.curseforge

import app.vercors.feature.project.backend.PlatformResponse
import app.vercors.feature.project.backend.curseforge.dto.*
import de.jensklingenberg.ktorfit.http.*

interface CurseForgeApi {
    @GET("v1/categories")
    suspend fun getCategories(
        @Query("gameId") gameId: Int,
        @Query("classId") classId: Int? = null,
        @Query("classesOnly") classesOnly: Boolean? = null
    ): PlatformResponse<CurseForgeStandardResponse<List<CurseForgeCategory>>>

    @GET("v1/mods/search")
    suspend fun search(
        @Query("gameId") gameId: Int,
        @Query("classId") classId: Int? = null,
        @Query("categoryIds") categoryIds: String? = null,
        @Query("gameVersions") gameVersions: String? = null,
        @Query("modLoaderTypes") modLoaderTypes: String? = null,
        @Query("searchFilter") searchFilter: String? = null,
        @Query("index") index: Int? = null,
        @Query("pageSize") pageSize: Int? = null,
        @Query("sortField") sortField: CurseForgeProjectSearchSortField? = CurseForgeProjectSearchSortField.Popularity,
        @Query("sortOrder") sortOrder: CurseForgeSortOrder? = CurseForgeSortOrder.Desc
    ): PlatformResponse<CurseForgeProjectSearchResponse>

    @GET("v1/mods/{projectId}")
    suspend fun getProject(@Path("projectId") projectId: String): PlatformResponse<CurseForgeStandardResponse<CurseForgeProject>>

    @GET("v1/mods/{projectId}/description")
    suspend fun getDescription(@Path("projectId") projectId: String): PlatformResponse<CurseForgeStandardResponse<String>>

    @GET("v1/mods/{projectId}/files")
    suspend fun getProjectFiles(
        @Path("projectId") projectId: String,
        @Query("gameVersion") gameVersion: String? = null,
        @Query("modLoaderType") modLoaderType: CurseForgeModLoaderType? = null,
        @Query("index") index: Int? = null,
        @Query("pageSize") pageSize: Int? = null
    ): PlatformResponse<CurseForgeStandardResponse<List<CurseForgeFile>>>

    @GET("v1/mods/{modId}/files/{fileId}")
    suspend fun getFile(
        @Path("modId") modId: String,
        @Path("fileId") fileId: String
    ): PlatformResponse<CurseForgeStandardResponse<CurseForgeFile>>

    @GET("v1/mods/{modId}/files/{fileId}/download-url")
    suspend fun getFileDownloadUrl(
        @Path("modId") modId: String,
        @Path("fileId") fileId: String
    ): PlatformResponse<CurseForgeStandardResponse<String>>

    @Headers("Content-Type: application/json")
    @POST("v1/mods/files")
    suspend fun getFiles(
        @Body request: CurseForgeGetFilesRequest
    ): PlatformResponse<CurseForgeStandardResponse<List<CurseForgeFile>>>

    @Headers("Content-Type: application/json")
    @POST("v1/mods")
    suspend fun getProjects(
        @Body request: CurseForgeGetModsRequest
    ): PlatformResponse<CurseForgeStandardResponse<List<CurseForgeProject>>>
}
