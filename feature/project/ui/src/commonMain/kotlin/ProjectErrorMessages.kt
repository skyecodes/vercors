package app.vercors.feature.project.ui

import androidx.compose.runtime.Composable
import app.vercors.feature.project.domain.model.PlatformError
import app.vercors.feature.project.domain.model.PlatformNotFound
import app.vercors.feature.project.domain.model.ProjectSearchError
import app.vercors.feature.project.domain.model.UnsupportedProjectType
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
