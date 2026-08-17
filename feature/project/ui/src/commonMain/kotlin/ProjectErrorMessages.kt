package app.vercors.feature.project.ui

import androidx.compose.runtime.Composable
import app.vercors.feature.project.domain.model.*
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
