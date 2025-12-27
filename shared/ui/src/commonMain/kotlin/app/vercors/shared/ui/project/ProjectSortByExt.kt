package app.vercors.shared.ui.project

import androidx.compose.runtime.Composable
import app.vercors.shared.domain.project.model.ProjectSortBy
import app.vercors.shared.ui.res.*
import org.jetbrains.compose.resources.stringResource

val ProjectSortBy.displayName: String
    @Composable get() = stringResource(
        when (this) {
            ProjectSortBy.Relevance -> Res.string.relevance
            ProjectSortBy.Downloads -> Res.string.downloads
            ProjectSortBy.DateUpdated -> Res.string.dateUpdated
            ProjectSortBy.DateCreated -> Res.string.dateCreated
        }
    )
