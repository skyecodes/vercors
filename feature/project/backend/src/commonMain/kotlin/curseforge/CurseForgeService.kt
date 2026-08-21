package app.vercors.feature.project.backend.curseforge

import app.vercors.feature.project.api.dto.*
import app.vercors.feature.project.backend.PlatformInfo
import app.vercors.feature.project.backend.PlatformServiceBase
import app.vercors.feature.project.backend.bindResponse
import arrow.core.raise.Raise
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.StringKey
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

internal const val MINECRAFT_GAME_ID = 432
internal const val MOD_CLASS_ID = 6
internal const val MODPACK_CLASS_ID = 4471
internal const val RESOURCEPACK_CLASS_ID = 12
internal const val SHADERPACK_CLASS_ID = 6552
internal const val DATAPACK_CLASS_ID = 6945
internal const val WORLD_CLASS_ID = 17

internal const val CURSEFORGE_PLATFORM_ID = "curseforge"

@SingleIn(AppScope::class)
@ContributesIntoMap(AppScope::class)
@StringKey(CURSEFORGE_PLATFORM_ID)
class CurseForgeService(
    private val api: CurseForgeApi
) : PlatformServiceBase {
    override val info = PlatformInfo(
        id = CURSEFORGE_PLATFORM_ID,
        name = "CurseForge",
        baseUrl = "https://www.curseforge.com/minecraft",
    )

    context(_: Raise<PlatformErrorData>)
    override suspend fun getCategories(): Map<ProjectTypeData, Map<String, List<ProjectCategoryData>>> =
        api.getCategories(
            gameId = MINECRAFT_GAME_ID
        ).bindResponse().data.let { categories ->
            categories.filter { it.isClass == true && it.convertToType() != null }.associate { type ->
                type.convertToType()!! to mapOf(
                    "categories" to categories
                        .filter { it.classId == type.id && it.parentCategoryId == type.id }
                        .map { it.convert() }
                        .sortedBy { it.name ?: it.slug }
                )
            }
        }

    context(_: Raise<UnsupportedProjectTypeData>, _: Raise<PlatformErrorData>)
    override suspend fun search(
        projectType: ProjectTypeData?,
        loaders: List<ProjectLoaderData>?,
        gameVersions: List<String>?,
        categories: List<String>?,
        query: String?,
        sortBy: ProjectSortByData?,
        offset: Int?,
        pageSize: Int?
    ): ProjectSearchResponseData = api.search(
        gameId = MINECRAFT_GAME_ID,
        classId = projectType?.toCurseForgeClassId(),
        categoryIds = categories?.joinToString(",", "[", "]") { "\"$it\"" },
        gameVersions = gameVersions?.joinToString(",", "[", "]") { "\"$it\"" },
        modLoaderTypes = loaders?.joinToString(",", "[", "]") { "\"${it.toCurseForgeModLoaderType().value}\"" },
        searchFilter = query,
        sortField = sortBy?.toCurseForgeSortField(),
        index = offset,
        pageSize = pageSize
    ).bindResponse().convert()

    context(_: Raise<PlatformErrorData>)
    override suspend fun getProject(projectId: String): ProjectData = coroutineScope {
        val project = async { api.getProject(projectId).bindResponse().data }
        val description = async { api.getDescription(projectId).bindResponse().data }
        project.await().convert(description.await())
    }

    context(_: Raise<PlatformErrorData>)
    override suspend fun getProjectFiles(
        projectId: String,
        gameVersion: String?,
        loader: ProjectLoaderData?
    ): List<ProjectFileData> {
        val files = api.getFiles(
            modId = projectId,
            gameVersion = gameVersion,
            modLoaderType = loader?.toCurseForgeModLoaderType()
        ).bindResponse().data
        val project =
            if (files.any { it.downloadUrl == null }) api.getProject(projectId).bindResponse().data else null
        return files.map { file ->
            val data = file.convert(loader)
            if (project != null && file.downloadUrl == null) data.copy(
                manualDownloadUrl = manualDownloadUrl(
                    project.slug,
                    project.classId,
                    file.id
                )
            ) else data
        }
    }

    context(_: Raise<PlatformErrorData>)
    override suspend fun getFileDownloadUrl(projectId: String, fileId: String): String {
        val file = api.getFile(projectId, fileId).bindResponse().data
        return file.downloadUrl ?: api.getFileDownloadUrl(projectId, fileId).bindResponse().data
    }

    context(_: Raise<PlatformErrorData>)
    override suspend fun getProjectFile(projectId: String, fileId: String): ProjectFileData {
        val file = api.getFile(projectId, fileId).bindResponse().data
        val data = file.convert(loaderFilter = null)
        if (file.downloadUrl != null) return data
        val project = api.getProject(projectId).bindResponse().data
        return data.copy(manualDownloadUrl = manualDownloadUrl(project.slug, project.classId, file.id))
    }

    private fun manualDownloadUrl(slug: String, classId: Int?, fileId: Int): String =
        "https://www.curseforge.com/minecraft/${classId.toCurseForgeUrlSegment()}/$slug/download/$fileId"
}
