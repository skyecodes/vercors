package app.vercors.feature.project.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.vercors.core.ui.animation.appSharedElement
import app.vercors.feature.project.domain.model.ProjectId
import com.slack.circuit.sharedelements.SharedElementTransitionScope

enum class ProjectSharedElementOrigin {
    Home, ProjectList
}

@Composable
context(scope: SharedElementTransitionScope)
fun Modifier.projectSharedElement(
    projectId: ProjectId,
    key: String,
    origin: ProjectSharedElementOrigin? = null
): Modifier = with(scope) {
    origin?.let {
        appSharedElement(
            sharedContentState = rememberSharedContentState("$it/project/$projectId/$key"),
            animatedVisibilityScope = requireAnimatedScope(SharedElementTransitionScope.AnimatedScope.Navigation)
        )
    } ?: ProjectSharedElementOrigin.entries.fold(this@projectSharedElement) { modifier, it ->
        modifier.projectSharedElement(projectId, key, it)
    }
}
