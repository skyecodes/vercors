package app.vercors.feature.project.domain.usecase

import app.vercors.feature.project.domain.model.modpack.InstalledModpackFile
import app.vercors.feature.project.domain.model.modpack.ModpackInstallError
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
        onProgress: (Progress) -> Unit = {}
    ): List<InstalledModpackFile> = projectRepository.installModpackFiles(manifest, instanceId, onProgress)
}
