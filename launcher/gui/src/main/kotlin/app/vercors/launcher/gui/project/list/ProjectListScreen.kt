package app.vercors.launcher.gui.project.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.vercors.launcher.gui.res.Res
import app.vercors.launcher.gui.res.search
import app.vercors.shared.domain.project.model.ProjectId
import app.vercors.shared.ui.component.AppIconTextButton
import app.vercors.shared.ui.component.AppSelectField
import app.vercors.shared.ui.component.AppText
import app.vercors.shared.ui.component.AppTextField
import app.vercors.shared.ui.modifier.appCard
import app.vercors.shared.ui.theme.colors
import app.vercors.shared.ui.theme.surface2
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Search
import com.composeunstyled.theme.Theme
import org.jetbrains.compose.resources.stringResource
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.compose.dsl.subscribe

@Composable
fun ProjectListScreen(
    container: ProjectListContainer,
    navigateToProjectDetails: (ProjectId) -> Unit
) = with(container.store) {
    val state by subscribe { action ->
        when (action) {
            is ProjectListAction.NavigateToProjectDetails -> navigateToProjectDetails(action.projectId)
        }
    }

    ProjectListScreen(state)
}

@Composable
context(store: IntentReceiver<ProjectListIntent>)
fun ProjectListScreen(state: ProjectListState) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        ProjectListFilters(state)
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {

        }
    }
}

@Composable
context(store: IntentReceiver<ProjectListIntent>)
private fun ProjectListFilters(state: ProjectListState) {
    Row(
        modifier = Modifier.padding(8.dp).appCard().padding(8.dp).fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AppSelectField(
            value = state.selectedProvider,
            items = state.providerList,
            onValueChange = { store.intent(ProjectListIntent.OnProjectProviderChange(it)) },
            textConverter = { it.name },
            backgroundColor = Theme[colors][surface2],
            modifier = Modifier.width(150.dp)
        )

        AppSelectField(
            value = state.selectedType,
            items = state.typeList,
            onValueChange = { store.intent(ProjectListIntent.OnProjectTypeChange(it)) },
            textConverter = { it.name },
            backgroundColor = Theme[colors][surface2],
            modifier = Modifier.width(150.dp)
        )

        AppTextField(
            placeholder = { AppText(stringResource(Res.string.search)) },
            initialValue = state.searchQuery,
            onValueChange = { store.intent(ProjectListIntent.OnSearchQueryChange(it)) },
            backgroundColor = Theme[colors][surface2],
            modifier = Modifier.weight(1f)
        )

        AppIconTextButton(
            onClick = { store.intent(ProjectListIntent.OnSearchButtonClick) },
            imageVector = Lucide.Search,
            text = stringResource(Res.string.search),
            backgroundColor = Theme[colors][surface2],
        )
    }
}
