package app.vercors.feature.project.domain.repository

import app.vercors.core.domain.Page
import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.project.domain.model.*
import app.vercors.feature.project.domain.model.modpack.InstalledModpackFile
import app.vercors.feature.project.domain.model.modpack.ModpackInstallError
import app.vercors.feature.project.domain.model.modpack.ModpackManifest
import app.vercors.feature.project.domain.model.modpack.ModpackResolutionError
import arrow.core.raise.Raise
import opensavvy.progress.Progress

interface ProjectRepository {
    context(_: Raise<PlatformError>)
    suspend fun getPlatformList(): List<ProjectPlatform>

    context(_: Raise<PlatformNotFound>, _: Raise<PlatformError>)
    suspend fun getFeaturedProjectList(platformId: PlatformId): FeaturedProjectList

    context(_: Raise<ProjectSearchError>)
    suspend fun searchProjects(
        platformId: PlatformId,
        type: ProjectType,
        loaders: List<ModLoaderType>?,
        gameVersions: List<String>?,
        query: String?,
        categories: List<String>?,
        sortBy: ProjectSortBy?,
        pageSize: Int?,
        offset: Int?
    ): Page<BriefProject>

    context(_: Raise<ProjectDetailsError>)
    suspend fun getProject(projectId: ProjectId): Project

    context(_: Raise<ProjectDetailsError>)
    suspend fun getProjectFiles(projectId: ProjectId, gameVersion: String?, loader: ModLoaderType?): List<ProjectFile>

    context(_: Raise<ProjectFileDownloadError>)
    suspend fun downloadProjectFiles(
        candidates: List<ProjectFileDownloadCandidate>,
        instanceId: String,
        type: ProjectType,
        onProgress: (Progress) -> Unit = {}
    )

    context(_: Raise<ProjectDetailsError>)
    suspend fun getProjectFile(projectId: ProjectId, fileId: String): ProjectFile

    context(_: Raise<ModpackResolutionError>)
    suspend fun getModpackManifest(
        projectId: ProjectId,
        fileId: String,
        onProgress: (Progress) -> Unit = {}
    ): ModpackManifest

    context(_: Raise<ModpackResolutionError>)
    suspend fun resolveModpackManifestFiles(
        manifest: ModpackManifest,
        onProgress: (Progress) -> Unit = {}
    ): ModpackManifest

    context(_: Raise<ModpackInstallError>)
    suspend fun installModpackFiles(
        manifest: ModpackManifest,
        instanceId: String,
        onProgress: (Progress) -> Unit
    ): List<InstalledModpackFile>

    /**
     * Suspends until [fileName] appears in the OS Downloads folder (or immediately if it's already there), then
     * moves it into the instance's directory for [type]. Cancelling the calling coroutine cancels the wait.
     */
    context(_: Raise<ManualDownloadError>)
    suspend fun awaitManualDownload(
        fileName: String,
        instanceId: String,
        type: ProjectType,
        size: Long?,
        sha1: String?,
        md5: String?
    )
}
