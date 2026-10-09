package app.vercors.feature.project.backend.curseforge

import app.vercors.feature.project.api.dto.*
import app.vercors.feature.project.backend.PlatformInfo
import app.vercors.feature.project.backend.PlatformServiceBase
import app.vercors.feature.project.backend.bindResponse
import app.vercors.feature.project.backend.curseforge.dto.CurseForgeFile
import app.vercors.feature.project.backend.curseforge.dto.CurseForgeGetFilesRequest
import app.vercors.feature.project.backend.curseforge.dto.CurseForgeGetModsRequest
import app.vercors.feature.project.backend.curseforge.dto.CurseForgeProject
import arrow.core.raise.Raise
import arrow.core.raise.recover
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.StringKey
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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
    ): List<ProjectFileData> = coroutineScope {
        val files = api.getProjectFiles(
            projectId = projectId,
            gameVersion = gameVersion,
            modLoaderType = loader?.toCurseForgeModLoaderType()
        ).bindResponse().data

        val downloadableIds = files.downloadableIds()
        val needsManualUrl = files.any { it.downloadUrl == null && it.id !in downloadableIds }
        val project = if (needsManualUrl) api.getProject(projectId).bindResponse().data else null

        files.map { file -> file.convert(loader).withManualDownloadUrl(file, project, downloadableIds) }
    }

    context(_: Raise<PlatformErrorData>)
    override suspend fun getFiles(fileIds: List<String>): List<ProjectFileData> = coroutineScope {
        val files = api.getFiles(CurseForgeGetFilesRequest(fileIds.map { it.toInt() })).bindResponse().data
        val downloadableIds = files.downloadableIds()

        // Also resolves each file's project type: the file listing never carries classId
        val projectsByModId = api.getProjects(CurseForgeGetModsRequest(files.map { it.modId }.distinct()))
            .bindResponse().data.associateBy { it.id }

        files.map { file ->
            val project = projectsByModId[file.modId]
            file.convert(loaderFilter = null)
                .copy(type = project?.classId.convertToType())
                .withManualDownloadUrl(file, project, downloadableIds)
        }
    }

    context(_: Raise<PlatformErrorData>)
    override suspend fun getFileDownloadUrl(projectId: String, fileId: String): String =
        api.getFile(projectId, fileId).bindResponse().data.downloadUrl
            ?: api.getFileDownloadUrl(projectId, fileId).bindResponse().data

    context(_: Raise<PlatformErrorData>)
    override suspend fun getProjectFile(projectId: String, fileId: String): ProjectFileData =
        api.getFile(projectId, fileId).bindResponse().data.let { file ->
            val data = file.convert(loaderFilter = null)
            if (file.downloadUrl != null || isDownloadable(projectId, fileId)) data
            else data.withManualDownloadUrl(api.getProject(projectId).bindResponse().data, file.id)
        }

    // A null downloadUrl doesn't always mean distribution-restricted: the dedicated endpoint can still succeed
    private suspend fun isDownloadable(projectId: String, fileId: String): Boolean = recover({
        api.getFileDownloadUrl(projectId, fileId).bindResponse()
        true
    }) { _: PlatformErrorData -> false }

    private suspend fun List<CurseForgeFile>.downloadableIds(): Set<Int> = coroutineScope {
        filter { it.downloadUrl == null }
            .map { file -> async { file.id.takeIf { isDownloadable(file.modId.toString(), file.id.toString()) } } }
            .awaitAll()
            .filterNotNull()
            .toSet()
    }

    private fun ProjectFileData.withManualDownloadUrl(
        file: CurseForgeFile,
        project: CurseForgeProject?,
        downloadableIds: Set<Int>
    ): ProjectFileData =
        if (project != null && file.downloadUrl == null && file.id !in downloadableIds) {
            withManualDownloadUrl(project, file.id)
        } else this

    private fun ProjectFileData.withManualDownloadUrl(project: CurseForgeProject, fileId: Int): ProjectFileData =
        copy(manualDownloadUrl = manualDownloadUrl(project.slug, project.classId, fileId))

    private fun manualDownloadUrl(slug: String, classId: Int?, fileId: Int): String =
        "https://www.curseforge.com/minecraft/${classId.toCurseForgeUrlSegment()}/$slug/download/$fileId"
}
