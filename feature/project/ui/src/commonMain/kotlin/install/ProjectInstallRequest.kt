package app.vercors.feature.project.ui.install

import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.project.domain.model.ProjectType
import app.vercors.feature.project.ui.BriefProjectView

sealed interface ProjectInstallTarget {
    data class Project(val dialog: InstallProjectDialog) : ProjectInstallTarget
    data class Modpack(val dialog: InstallModpackDialog) : ProjectInstallTarget
}

fun BriefProjectView.toInstallTarget(
    preferredInstanceId: InstanceId?,
    installProjectDialogFactory: InstallProjectDialog.Factory,
    installModpackDialogFactory: InstallModpackDialog.Factory,
): ProjectInstallTarget = if (type == ProjectType.Modpack) {
    ProjectInstallTarget.Modpack(installModpackDialogFactory.create(id, name))
} else {
    ProjectInstallTarget.Project(installProjectDialogFactory.create(id, type, loaders, preferredInstanceId))
}
