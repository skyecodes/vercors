package app.vercors.launcher.gui.instance.list

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import app.vercors.launcher.gui.instance.BriefInstanceCard
import app.vercors.launcher.gui.instance.InstanceSharedElementOrigin
import app.vercors.launcher.gui.res.*
import app.vercors.shared.ui.core.animation.appAnimateItem
import app.vercors.shared.ui.core.component.*
import app.vercors.shared.ui.core.modifier.appCard
import app.vercors.shared.ui.core.theme.colors
import app.vercors.shared.ui.core.theme.onPrimary
import app.vercors.shared.ui.core.theme.primary
import app.vercors.shared.ui.core.theme.surface2
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.composeunstyled.theme.Theme
import org.jetbrains.compose.resources.stringResource
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.compose.dsl.subscribe

@Composable
context(
    _: AnimatedContentScope,
    _: SharedTransitionScope
)
fun InstanceListScreen(
    container: InstanceListContainer,
    navigateToCreateInstance: () -> Unit,
    navigateToInstanceDetails: (String) -> Unit
) = with(container.store) {
    val state by subscribe { action ->
        when (action) {
            InstanceListAction.NavigateToCreateInstance -> navigateToCreateInstance()
            is InstanceListAction.NavigateToInstanceDetails -> navigateToInstanceDetails(action.instanceId)
        }
    }

    InstanceListScreen(state)
}

@Composable
context(
    store: IntentReceiver<InstanceListIntent>,
    _: AnimatedContentScope,
    _: SharedTransitionScope
)
private fun InstanceListScreen(state: InstanceListState) {
    Column(
        modifier = Modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        InstanceListFilterCard(state)

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            state.filteredInstances.forEach { instance ->
                item(instance.id) {
                    BriefInstanceCard(
                        instance = instance,
                        onInstanceClick = { store.intent(InstanceListIntent.OnInstanceClick(it)) },
                        modifier = Modifier.fillMaxWidth().height(150.dp).appAnimateItem(),
                        origin = InstanceSharedElementOrigin.InstanceList
                    )
                }
            }
        }
    }
}

@Composable
context(store: IntentReceiver<InstanceListIntent>)
fun InstanceListFilterCard(state: InstanceListState) {
    Row(
        modifier = Modifier.appCard().padding(8.dp).fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIconTextButton(
            onClick = { store.intent(InstanceListIntent.OnCreateInstanceClick) },
            text = stringResource(Res.string.create),
            imageVector = Lucide.Plus,
            backgroundColor = Theme[colors][primary],
            contentColor = Theme[colors][onPrimary],
        )

        AppSelectField(
            value = state.typeFilter,
            items = state.typeList,
            onValueChange = { store.intent(InstanceListIntent.OnTypeFilterChange(it)) },
            itemRenderer = { AppSelectItem(it.ifBlank { stringResource(Res.string.anyType) }) },
            backgroundColor = Theme[colors][surface2],
            modifier = Modifier.width(150.dp)
        )

        AppSelectField(
            value = state.loaderFilter,
            items = state.loaderList,
            onValueChange = { store.intent(InstanceListIntent.OnLoaderFilterChange(it)) },
            itemRenderer = { AppSelectItem(it.ifBlank { stringResource(Res.string.anyLoader) }) },
            backgroundColor = Theme[colors][surface2],
            modifier = Modifier.width(150.dp)
        )

        AppSelectField(
            value = state.versionFilter,
            items = state.versionList,
            onValueChange = { store.intent(InstanceListIntent.OnVersionFilterChange(it)) },
            itemRenderer = { AppSelectItem(it.ifBlank { stringResource(Res.string.anyVersion) }) },
            backgroundColor = Theme[colors][surface2],
            modifier = Modifier.width(150.dp)
        )

        AppTextField(
            placeholder = { AppText(stringResource(Res.string.search)) },
            initialValue = state.nameFilter,
            onValueChange = { store.intent(InstanceListIntent.OnNameFilterChange(it)) },
            imeAction = ImeAction.Search,
            backgroundColor = Theme[colors][surface2],
            modifier = Modifier.weight(1f)
        )
    }
}
