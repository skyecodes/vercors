package app.vercors.feature.project.backend.modrinth

import app.vercors.feature.project.api.dto.*
import app.vercors.feature.project.backend.PlatformInfo
import app.vercors.feature.project.backend.PlatformServiceBase
import app.vercors.shared.backend.bindResponse
import arrow.core.raise.context.Raise
import arrow.core.raise.context.ensureNotNull
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.StringKey
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

internal const val MODRINTH_PLATFORM_ID = "modrinth"

@SingleIn(AppScope::class)
@ContributesIntoMap(AppScope::class)
@StringKey(MODRINTH_PLATFORM_ID)
class ModrinthService(
    private val api: ModrinthApi
) : PlatformServiceBase {
    override val info: PlatformInfo = PlatformInfo(
        id = MODRINTH_PLATFORM_ID,
        name = "Modrinth",
        baseUrl = "https://modrinth.com/"
    )

    val pattern = Regex("^(\\d+).*$")

    context(_: Raise<PlatformErrorData>)
    override suspend fun getCategories(): Map<ProjectTypeData, Map<String, List<ProjectCategoryData>>> =
        coroutineScope {
            val types = async { api.getProjectTypes().bindResponse().mapNotNull { it.convert() } }
            val categories = async { api.getCategories().bindResponse() }

            val result = categories.await()
                .groupBy { it.projectType.convert() }
                .filterKeys { it in types.await() }
                .map { (type, cats) ->
                    type!! to cats.groupBy { it.header }.mapValues { (group, list) ->
                        list.map { it.convert() }.let { mappedList ->
                            if (group == "resolutions")
                                mappedList.sortedBy { pattern.matchEntire(it.slug)?.groups[1]?.value?.toInt() }
                            else
                                mappedList.sortedBy { it.slug }
                        }
                    }
                }
                .toMap()
            result + (ProjectTypeData.DataPack to result.getValue(ProjectTypeData.Mod)
                .filterKeys { it == "categories" })
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
        query = query,
        facets = buildFacets(
            projectType = projectType,
            loaders = loaders,
            gameVersions = gameVersions,
            categories = categories
        ),
        index = sortBy?.toModrinthIndex(),
        offset = offset ?: 0,
        limit = pageSize
    ).bindResponse().convert()

    context(_: Raise<PlatformErrorData>)
    override suspend fun getProject(projectId: String): ProjectData = coroutineScope {
        val project = async { api.getProject(projectId).bindResponse() }
        val members = async { api.getProjectMembers(projectId).bindResponse() }
        project.await().convert(members.await())
    }

    context(_: Raise<PlatformErrorData>)
    override suspend fun getProjectFiles(
        projectId: String,
        gameVersion: String?,
        loader: ProjectLoaderData?
    ): List<ProjectFileData> = api.getVersions(
        projectId = projectId,
        loaders = loader?.let { "[\"${it.toModrinthString()}\"]" },
        gameVersions = gameVersion?.let { "[\"$it\"]" }
    ).bindResponse().mapNotNull { it.convert() }

    context(_: Raise<PlatformErrorData>)
    override suspend fun getFileDownloadUrl(projectId: String, fileId: String): String =
        ensureNotNull(api.getVersion(fileId).bindResponse().primaryFileOrNull()) { noFiles(fileId) }.url

    context(_: Raise<PlatformErrorData>)
    override suspend fun getProjectFile(projectId: String, fileId: String): ProjectFileData =
        ensureNotNull(api.getVersion(fileId).bindResponse().convert()) { noFiles(fileId) }

    context(_: Raise<PlatformErrorData>)
    override suspend fun getFiles(fileIds: List<String>): List<ProjectFileData> =
        api.getVersionsByIds(fileIds.joinToString(",", "[", "]") { "\"$it\"" }).bindResponse()
            .mapNotNull { it.convert() }

    private fun noFiles(fileId: String): PlatformErrorData =
        PlatformErrorData("NoFiles", "Version $fileId has no files")
}
