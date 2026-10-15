package app.vercors.feature.project.domain.usecase

import app.vercors.feature.project.domain.model.modpack.ModpackInstallError
import app.vercors.feature.project.domain.model.modpack.ModpackInstallResult
import app.vercors.feature.project.domain.model.modpack.ModpackManifest
import app.vercors.feature.project.domain.repository.ProjectRepository
import arrow.core.raise.Raise
import dev.zacsweers.metro.Inject
import opensavvy.progress.Progress

@Inject
class InstallModpackFilesUseCase(
    private val projectRepository: ProjectRepository
) {
    context(_: Raise<ModpackInstallError>)
    suspend operator fun invoke(
        manifest: ModpackManifest,
        instanceId: String,
        localSourcePath: String? = null,
        preserveUserState: Boolean = false,
        onProgress: (Progress) -> Unit = {}
    ): ModpackInstallResult =
        projectRepository.installModpackFiles(manifest, instanceId, localSourcePath, preserveUserState, onProgress)
}
