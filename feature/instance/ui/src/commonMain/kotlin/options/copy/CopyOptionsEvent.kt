package app.vercors.feature.instance.ui.options.copy

import app.vercors.feature.instance.domain.model.options.OptionsCategory
import app.vercors.shared.ui.Event

sealed interface CopyOptionsEvent : Event {
    data class OnSourceSelected(val source: CopyOptionsSourceView) : CopyOptionsEvent
    data class OnCategoryToggle(val category: OptionsCategory, val checked: Boolean) : CopyOptionsEvent
    data object OnCopyClick : CopyOptionsEvent
    data object OnCancelClick : CopyOptionsEvent
}
