package app.vercors.feature.project.ui.install

import androidx.compose.runtime.Immutable
import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.project.domain.model.ProjectDetailsError
import app.vercors.feature.project.domain.model.ProjectReleaseChannel
import opensavvy.state.progressive.ProgressiveOutcome
import opensavvy.state.progressive.valueOrNull
import kotlin.time.Instant

@Immutable
data class InstallModState(
    val instances: ProgressiveOutcome<Nothing, List<InstallableInstanceView>>,
    val selectedInstanceId: InstanceId?,
    val files: ProgressiveOutcome<ProjectDetailsError, List<ProjectFileView>>,
    val selectedFileId: String?,
    val installing: Boolean,
) {
    val instanceList = instances.valueOrNull ?: emptyList()
    val selectedInstance = instanceList.find { it.id == selectedInstanceId }
    val fileList = files.valueOrNull ?: emptyList()
    val selectedFile = fileList.find { it.id == selectedFileId }
    val canInstall = selectedInstance != null && selectedFile != null && !installing
}

@Immutable
data class InstallableInstanceView(
    val id: InstanceId,
    val name: String,
    val gameVersion: String,
    val loaderType: ModLoaderType,
    val loaderVersion: String,
)

@Immutable
data class ProjectFileView(
    val id: String,
    val displayName: String,
    val fileName: String,
    val gameVersions: List<String>,
    val releaseChannel: ProjectReleaseChannel,
    val publishedAt: Instant,
)
