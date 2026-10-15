package app.vercors.feature.instance.ui.options.copy

import androidx.compose.runtime.Immutable
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.model.options.FILE_OPTIONS_CATEGORIES
import app.vercors.feature.instance.domain.model.options.OptionsCategory
import app.vercors.feature.instance.domain.model.options.OptionsSource

@Immutable
data class CopyOptionsState(
    val targetGameVersion: String,
    val sources: List<CopyOptionsSourceView>?,
    val selectedSource: CopyOptionsSourceView?,
    val categories: Set<OptionsCategory>,
    val isCopying: Boolean = false,
) {
    val canCopy: Boolean = selectedSource != null && categories.isNotEmpty() && !isCopying
    val replacesFiles: Boolean = categories.any { it in FILE_OPTIONS_CATEGORIES }
    val versionMismatch: Boolean = selectedSource != null && selectedSource.gameVersion != targetGameVersion
}

// A null name is the launcher's default options
@Immutable
data class CopyOptionsSourceView(
    val source: OptionsSource,
    val name: String?,
    val gameVersion: String,
)

data class CopyOptionsTarget(val id: InstanceId, val gameVersion: String)
