package app.vercors.feature.project.domain.repository

import app.vercors.feature.project.domain.model.ProjectId
import app.vercors.feature.project.domain.model.modpack.ModpackInstallError
import app.vercors.feature.project.domain.model.modpack.ModpackInstallResult
import app.vercors.feature.project.domain.model.modpack.ModpackManifest
import app.vercors.feature.project.domain.model.modpack.ModpackResolutionError
import arrow.core.raise.Raise
import opensavvy.progress.Progress

interface ModpackRepository {
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

    context(_: Raise<ModpackResolutionError>)
    suspend fun parseLocalModpack(sourcePath: String, platformId: String): ModpackManifest

    context(_: Raise<ModpackInstallError>)
    suspend fun installModpackFiles(
        manifest: ModpackManifest,
        instanceId: String,
        localSourcePath: String? = null,
        preserveUserState: Boolean = false,
        onProgress: (Progress) -> Unit = {}
    ): ModpackInstallResult
}
