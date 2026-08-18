package app.vercors.feature.project.backend

import app.vercors.feature.project.api.dto.*
import arrow.core.Either.Companion.catch
import arrow.core.raise.Raise
import arrow.core.raise.context.bind
import arrow.core.raise.context.ensureNotNull
import arrow.core.raise.context.raise
import arrow.core.raise.recover
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

@SingleIn(AppScope::class)
@Inject
class ProjectService(
    private val platformServices: Map<String, PlatformServiceBase>
) {
    context(_: Raise<PlatformNotFoundData>)
    private fun getPlatformService(platformId: String): PlatformServiceBase =
        ensureNotNull(platformServices[platformId]) { PlatformNotFoundData(platformId) }

    context(_: Raise<PlatformErrorData>)
    suspend fun getPlatformList(): List<ProjectPlatformData> =
        platformServices.map { (id, it) -> id to it.getPlatformData() }.sortedBy { it.first }.map { it.second }

    context(_: Raise<PlatformNotFoundData>, _: Raise<PlatformErrorData>)
    suspend fun getPlatformDetails(platformId: String): ProjectPlatformData =
        getPlatformService(platformId).getPlatformData()

    context(_: Raise<PlatformNotFoundData>, _: Raise<PlatformErrorData>)
    suspend fun getFeaturedProjectList(platformId: String): FeaturedProjectListData = withContext(Dispatchers.IO) {
        getPlatformService(platformId).getCategories().keys.map { projectType ->
            async {
                projectType to recover({
                    search(platformId = platformId, projectType = projectType).results
                }) { e: UnsupportedProjectTypeData ->
                    raise(
                        PlatformErrorData(
                            "UnsupportedProjectTypeData",
                            "Unsupported project type ${e.projectType} yet it is present in the platform categories"
                        )
                    )
                }
            }
        }.awaitAll().toMap()
    }

    context(_: Raise<UnsupportedProjectTypeData>, _: Raise<PlatformNotFoundData>, _: Raise<PlatformErrorData>)
    suspend fun search(
        platformId: String,
        projectType: ProjectTypeData? = null,
        loaders: List<ProjectLoaderData>? = null,
        gameVersions: List<String>? = null,
        categories: List<String>? = null,
        query: String? = null,
        sortBy: ProjectSortByData? = null,
        offset: Int? = 0,
        pageSize: Int? = 10
    ): ProjectSearchResponseData = getPlatformService(platformId).search(
        projectType = projectType,
        loaders = loaders,
        gameVersions = gameVersions,
        categories = categories,
        query = query,
        sortBy = sortBy,
        offset = offset,
        pageSize = pageSize,
    )

    context(_: Raise<PlatformOrProjectNotFoundData>, _: Raise<PlatformErrorData>)
    suspend fun getProjectDetails(platformId: String, projectId: String) =
        catch { getPlatformService(platformId).getProject(projectId) }
            .mapLeft { ProjectNotFoundData(projectId, platformId) }
            .bind()

    context(_: Raise<PlatformOrProjectNotFoundData>, _: Raise<PlatformErrorData>)
    suspend fun getProjectFiles(
        platformId: String,
        projectId: String,
        gameVersion: String?,
        loader: ProjectLoaderData?
    ) = catch { getPlatformService(platformId).getProjectFiles(projectId, gameVersion, loader) }
        .mapLeft { ProjectNotFoundData(projectId, platformId) }
        .bind()

    context(_: Raise<PlatformOrProjectNotFoundData>, _: Raise<PlatformErrorData>)
    suspend fun getFileDownloadUrl(platformId: String, projectId: String, fileId: String) =
        catch { getPlatformService(platformId).getFileDownloadUrl(projectId, fileId) }
            .mapLeft { ProjectNotFoundData(projectId, platformId) }
            .bind()
}
