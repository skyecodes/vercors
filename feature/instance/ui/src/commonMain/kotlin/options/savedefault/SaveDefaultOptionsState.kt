package app.vercors.feature.instance.ui.options.savedefault

import androidx.compose.runtime.Immutable
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.model.options.OptionsCategory

@Immutable
data class SaveDefaultOptionsState(
    val categories: Set<OptionsCategory>,
    val replacedSourceName: String?,
    val isSaving: Boolean = false,
) {
    val canSave: Boolean = categories.isNotEmpty() && !isSaving
}

data class SaveDefaultOptionsTarget(val id: InstanceId)
