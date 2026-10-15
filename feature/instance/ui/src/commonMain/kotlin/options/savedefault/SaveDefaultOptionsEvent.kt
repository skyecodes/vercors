package app.vercors.feature.instance.ui.options.savedefault

import app.vercors.feature.instance.domain.model.options.OptionsCategory
import app.vercors.shared.ui.Event

sealed interface SaveDefaultOptionsEvent : Event {
    data class OnCategoryToggle(val category: OptionsCategory, val checked: Boolean) : SaveDefaultOptionsEvent
    data object OnSaveClick : SaveDefaultOptionsEvent
    data object OnCancelClick : SaveDefaultOptionsEvent
}
