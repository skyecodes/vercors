package app.vercors.feature.instance.ui.list

import androidx.compose.runtime.Immutable
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.ui.BriefInstanceView
import app.vercors.feature.instance.ui.copy.CopyInstanceDialog
import app.vercors.feature.instance.ui.create.CreateInstanceDialog
import app.vercors.feature.instance.ui.export.ExportInstanceDialog
import opensavvy.state.progressive.ProgressiveOutcome
import opensavvy.state.progressive.valueOrNull

@Immutable
data class InstanceListState(
    val instances: ProgressiveOutcome<Nothing, List<BriefInstanceView>>,
    val nameFilter: String,
    val versionFilter: String,
    val loaderFilter: String,
    val typeFilter: String,
    val promptCreateInstance: Boolean,
    val createInstanceDialog: CreateInstanceDialog,
    val pendingDeleteInstanceId: InstanceId?,
    val copyInstanceDialog: CopyInstanceDialog?,
    val exportInstanceDialog: ExportInstanceDialog?,
) {
    private val loadedInstances = instances.valueOrNull ?: emptyList()
    val filteredInstances = loadedInstances.filter {
        it.name.contains(nameFilter, ignoreCase = true) &&
                (typeFilter.isBlank() || it.type == typeFilter) &&
                (versionFilter.isBlank() || it.version == versionFilter) &&
                (loaderFilter.isBlank() || it.loader == loaderFilter)
    }
    val versionList = listOf("") + loadedInstances.map { it.version }.distinct()
    val loaderList = listOf("") + loadedInstances.map { it.loader }.distinct()
    val typeList = listOf("") + loadedInstances.map { it.type }.distinct()
}
