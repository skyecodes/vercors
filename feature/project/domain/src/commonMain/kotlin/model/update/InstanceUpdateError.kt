package app.vercors.feature.project.domain.model.update

import app.vercors.feature.project.domain.model.ProjectDetailsError
import app.vercors.feature.project.domain.model.modpack.ModpackResolutionError
import kotlin.jvm.JvmInline

sealed interface InstanceUpdateError {
    @JvmInline
    value class ModpackLookupFailed(val error: ProjectDetailsError) : InstanceUpdateError

    @JvmInline
    value class ManifestFailed(val error: ModpackResolutionError) : InstanceUpdateError

    data object InstanceNotFound : InstanceUpdateError

    data object SaveFailed : InstanceUpdateError

    data object DownloadFailed : InstanceUpdateError
}
