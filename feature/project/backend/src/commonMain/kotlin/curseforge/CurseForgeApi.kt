package app.vercors.feature.project.backend.curseforge

import app.vercors.feature.project.backend.PlatformResponse
import app.vercors.feature.project.backend.curseforge.dto.*
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query

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

    @GET("v1/mods/{modId}/description")
    suspend fun getDescription(@Path("modId") modId: String): PlatformResponse<CurseForgeStandardResponse<String>>
}
