package app.vercors.feature.project.ui

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.vercors.core.ui.animation.appSharedElement
import app.vercors.feature.project.domain.model.ProjectId

enum class ProjectSharedElementOrigin {
    Home, ProjectList
}

@Composable
context(
    animatedVisibilityScope: AnimatedContentScope,
    sharedTransitionScope: SharedTransitionScope
)
fun Modifier.projectSharedElement(
    projectId: ProjectId,
    key: String,
    origin: ProjectSharedElementOrigin? = null
): Modifier = with(sharedTransitionScope) {
    origin?.let {
        appSharedElement(
            sharedContentState = rememberSharedContentState("$it/project/$projectId/$key"),
            animatedVisibilityScope = animatedVisibilityScope
        )
    } ?: ProjectSharedElementOrigin.entries.fold(this@projectSharedElement) { modifier, it ->
        modifier.projectSharedElement(projectId, key, it)
    }
}
