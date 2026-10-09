package app.vercors.feature.project.ui

import androidx.compose.runtime.Composable
import app.vercors.feature.instance.domain.model.InstanceValidationError
import app.vercors.feature.project.domain.model.*
import app.vercors.feature.project.domain.model.modpack.ModpackInstallError
import app.vercors.feature.project.domain.model.modpack.ModpackResolutionError
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
        ModpackResolutionError.DownloadFailed -> stringResource(ProjectRes.string.errorModpackDownloadFailed)
        ModpackResolutionError.ParseFailed -> stringResource(ProjectRes.string.errorModpackParseFailed)
        ModpackResolutionError.UnsupportedPlatform -> stringResource(ProjectRes.string.errorModpackUnsupportedPlatform)
        ModpackResolutionError.FileResolutionFailed -> stringResource(ProjectRes.string.errorModpackFileResolutionFailed)
    }

val ModpackInstallError.message: String
    @Composable get() = when (this) {
        ModpackInstallError.DownloadFailed -> stringResource(ProjectRes.string.errorModpackInstallDownloadFailed)
        ModpackInstallError.OverridesExtractionFailed -> stringResource(ProjectRes.string.errorModpackOverridesExtractionFailed)
        ModpackInstallError.InvalidFileEntry -> stringResource(ProjectRes.string.errorModpackInvalidFileEntry)
    }

val InstanceValidationError.message: String
    @Composable get() = stringResource(ProjectRes.string.errorInstanceNameInvalid)
