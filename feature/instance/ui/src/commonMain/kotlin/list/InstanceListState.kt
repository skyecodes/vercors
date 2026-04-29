package app.vercors.feature.instance.ui.list

import androidx.compose.runtime.Immutable
import app.vercors.feature.instance.ui.BriefInstanceView
import opensavvy.state.progressive.ProgressiveOutcome
import opensavvy.state.progressive.valueOrNull

@Immutable
data class InstanceListState(
    val instances: ProgressiveOutcome<Nothing, List<BriefInstanceView>>,
    val nameFilter: String,
    val versionFilter: String,
    val loaderFilter: String,
    val typeFilter: String
) {
    private val loadedInstances = instances.valueOrNull ?: emptyList()
    val filteredInstances = loadedInstances.filter {
        it.name.contains(nameFilter, ignoreCase = true) &&
                (versionFilter.isBlank() || it.version == versionFilter) &&
                (loaderFilter.isBlank() || it.loader == loaderFilter)
    }
    val versionList = listOf("") + loadedInstances.map { it.version }.distinct()
    val loaderList = listOf("") + loadedInstances.map { it.loader }.distinct()
    val typeList = listOf("") + loadedInstances.map { it.type }.distinct()
}
