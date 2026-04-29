package app.vercors.feature.home.ui

import androidx.compose.runtime.Composable
import app.vercors.feature.home.domain.model.HomeSectionError
import app.vercors.feature.project.ui.ProjectRes
import app.vercors.feature.project.ui.errorPlatform
import app.vercors.feature.project.ui.errorPlatformNotFound
import org.jetbrains.compose.resources.stringResource

val HomeSectionError.message: String
    @Composable get() = when (this) {
        is HomeSectionError.PlatformNotFound -> stringResource(ProjectRes.string.errorPlatformNotFound, platformId)
        is HomeSectionError.PlatformError -> stringResource(
            ProjectRes.string.errorPlatform,
            error,
            description ?: "No description"
        )
    }
