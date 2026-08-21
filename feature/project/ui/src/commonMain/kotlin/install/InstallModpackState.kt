package app.vercors.feature.project.ui.install

import androidx.compose.runtime.Immutable
import app.vercors.feature.project.domain.model.ProjectDetailsError
import opensavvy.state.progressive.ProgressiveOutcome
import opensavvy.state.progressive.valueOrNull

@Immutable
data class InstallModpackState(
    val files: ProgressiveOutcome<ProjectDetailsError, List<ProjectFileView>>,
    val selectedFileId: String?,
    val instanceName: String,
    val installing: Boolean,
) {
    val fileList = files.valueOrNull ?: emptyList()
    val selectedFile = fileList.find { it.id == selectedFileId }
    val canInstall = selectedFile != null && instanceName.length >= 3 && !installing
}
