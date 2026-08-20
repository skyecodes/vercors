package app.vercors.feature.project.ui

import androidx.compose.runtime.Composable
import app.vercors.feature.instance.domain.model.InstanceValidationError
import app.vercors.feature.project.domain.model.*
import app.vercors.feature.project.domain.model.modpack.*
import app.vercors.feature.project.ui.list.displayName
import org.jetbrains.compose.resources.stringResource

val ProjectSearchError.message: String
    @Composable get() = when (this) {
        is PlatformError -> error + (description?.let { ": $it" } ?: "")
        is PlatformNotFound -> stringResource(ProjectRes.string.errorPlatformNotFound, platformId)
        is UnsupportedProjectType -> stringResource(
            ProjectRes.string.errorUnsupportedProjectType,
            projectType.displayName
        )
    }

val ProjectDetailsError.message: String
    @Composable get() = when (this) {
        is PlatformError -> error + (description?.let { ": $it" } ?: "")
        is PlatformNotFound -> stringResource(ProjectRes.string.errorPlatformNotFound, platformId)
        is ProjectNotFound -> stringResource(ProjectRes.string.errorProjectNotFound, projectId.id)
    }

val ProjectFileDownloadError.message: String
    @Composable get() = when (this) {
        ProjectFileDownloadFailed -> stringResource(ProjectRes.string.errorProjectFileDownloadFailed)
    }

val ModpackResolutionError.message: String
    @Composable get() = when (this) {
        ModpackDownloadFailed -> stringResource(ProjectRes.string.errorModpackDownloadFailed)
        ModpackParseFailed -> stringResource(ProjectRes.string.errorModpackParseFailed)
        ModpackUnsupportedPlatform -> stringResource(ProjectRes.string.errorModpackUnsupportedPlatform)
        ModpackFileResolutionFailed -> stringResource(ProjectRes.string.errorModpackFileResolutionFailed)
    }

val ModpackInstallError.message: String
    @Composable get() = when (this) {
        ModpackInstallDownloadFailed -> stringResource(ProjectRes.string.errorModpackInstallDownloadFailed)
        ModpackOverridesExtractionFailed -> stringResource(ProjectRes.string.errorModpackOverridesExtractionFailed)
    }

val InstanceValidationError.message: String
    @Composable get() = stringResource(ProjectRes.string.errorInstanceNameInvalid)

val ManualDownloadError.message: String
    @Composable get() = stringResource(ProjectRes.string.errorManualDownloadFailed)
