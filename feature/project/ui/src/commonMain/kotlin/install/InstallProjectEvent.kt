package app.vercors.feature.project.ui.install

import app.vercors.feature.game.domain.model.LoaderVersion
import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.game.ui.GameVersionView
import app.vercors.feature.project.domain.model.ProjectId
import app.vercors.shared.ui.Event

sealed interface InstallProjectEvent : Event {
    data class OnInstanceSelectorItemSelected(val item: InstanceSelectorItem?) : InstallProjectEvent
    data class OnNewInstanceNameChange(val name: String) : InstallProjectEvent
    data class OnNewGameVersionChange(val version: GameVersionView?) : InstallProjectEvent
    data class OnNewLoaderChange(val loader: ModLoaderType?) : InstallProjectEvent
    data class OnNewLoaderVersionChange(val version: LoaderVersion?) : InstallProjectEvent
    data class OnFileSelected(val fileId: String?) : InstallProjectEvent
    data class OnDependencyToggled(val projectId: ProjectId, val checked: Boolean) : InstallProjectEvent
    data class OnCopyUrlClick(val url: String) : InstallProjectEvent
    data object OnConfirmClick : InstallProjectEvent
    data object OnCancelClick : InstallProjectEvent
    data object OnCloseClick : InstallProjectEvent
    data object OnGoToInstanceClick : InstallProjectEvent
}
