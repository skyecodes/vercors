package app.vercors.feature.instance.ui.list

import androidx.compose.runtime.Immutable
import app.vercors.core.domain.Resource
import app.vercors.core.domain.orNull
import app.vercors.feature.instance.ui.BriefInstanceView

@Immutable
data class InstanceListState(
    val instances: Resource<List<BriefInstanceView>> = Resource.Unloaded,
    val nameFilter: String = "",
    val versionFilter: String = "",
    val loaderFilter: String = "",
    val typeFilter: String = ""
) {
    val loadedInstances = instances.orNull() ?: emptyList()
    val filteredInstances = loadedInstances.filter {
        it.name.contains(nameFilter, ignoreCase = true) &&
                (versionFilter.isBlank() || it.version == versionFilter) &&
                (loaderFilter.isBlank() || it.loader == loaderFilter)
    }
    val versionList = listOf("") + loadedInstances.map { it.version }.distinct()
    val loaderList = listOf("") + loadedInstances.map { it.loader }.distinct()
    val typeList = listOf("") + loadedInstances.map { it.type }.distinct()
}
