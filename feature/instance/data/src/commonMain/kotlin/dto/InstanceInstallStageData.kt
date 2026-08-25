package app.vercors.feature.instance.data.dto

import app.vercors.feature.instance.domain.model.InstanceInstallStage
import kotlinx.serialization.Serializable

@Serializable
enum class InstanceInstallStageData {
    ResolvingFiles, DownloadingFiles
}

fun InstanceInstallStageData.toDomain(): InstanceInstallStage = when (this) {
    InstanceInstallStageData.ResolvingFiles -> InstanceInstallStage.ResolvingFiles
    InstanceInstallStageData.DownloadingFiles -> InstanceInstallStage.DownloadingFiles
}

fun InstanceInstallStage.toData(): InstanceInstallStageData = when (this) {
    InstanceInstallStage.ResolvingFiles -> InstanceInstallStageData.ResolvingFiles
    InstanceInstallStage.DownloadingFiles -> InstanceInstallStageData.DownloadingFiles
}
