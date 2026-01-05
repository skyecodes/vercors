package app.vercors.feature.project.ui.details

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.vercors.core.ui.component.AppText
import app.vercors.shared.ui.Store
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.zacsweers.metro.AppScope

@CircuitInject(ProjectDetailsScreen::class, AppScope::class)
@Composable
fun ProjectDetails(
    store: Store<ProjectDetailsState, ProjectDetailsEvent>,
    modifier: Modifier = Modifier
) {
    val (state, eventSink) = store

    with(eventSink) {
        AppText(
            text = state.projectId.toString(),
            modifier = modifier
        )
    }
}
