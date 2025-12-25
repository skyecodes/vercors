package app.vercors.launcher.gui.project

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.vercors.shared.domain.project.model.ProjectId
import app.vercors.shared.ui.animation.appSharedElement

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
