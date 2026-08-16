package app.vercors.feature.instance.ui.create

import app.vercors.feature.game.domain.model.LoaderVersion
import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.game.ui.GameVersionView
import app.vercors.shared.ui.Event

sealed interface CreateInstanceEvent : Event {
    data class OnInstanceNameChange(val name: String) : CreateInstanceEvent
    data class OnGameVersionChange(val version: GameVersionView?) : CreateInstanceEvent
    data class OnLoaderChange(val loader: ModLoaderType?) : CreateInstanceEvent
    data class OnLoaderVersionChange(val version: LoaderVersion?) : CreateInstanceEvent
    data object OnCreateInstanceClick : CreateInstanceEvent
    data object OnCancelClick : CreateInstanceEvent
}
