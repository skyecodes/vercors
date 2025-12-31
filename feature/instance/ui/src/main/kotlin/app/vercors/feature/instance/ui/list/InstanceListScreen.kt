package app.vercors.feature.instance.ui.list

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
import app.vercors.core.ui.CoreRes
import app.vercors.core.ui.animation.appAnimateItem
import app.vercors.core.ui.component.*
import app.vercors.core.ui.modifier.appCard
import app.vercors.core.ui.search
import app.vercors.core.ui.theme.colors
import app.vercors.core.ui.theme.onPrimary
import app.vercors.core.ui.theme.primary
import app.vercors.core.ui.theme.surface2
import app.vercors.feature.instance.ui.*
import app.vercors.shared.ui.SharedRes
import app.vercors.shared.ui.create
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
            text = stringResource(SharedRes.string.create),
            imageVector = Lucide.Plus,
            backgroundColor = Theme[colors][primary],
            contentColor = Theme[colors][onPrimary]
        )

        AppSelectField(
            value = state.typeFilter,
            items = state.typeList,
            onValueChange = { store.intent(InstanceListIntent.OnTypeFilterChange(it)) },
            itemRenderer = { value, _ -> AppSelectItem(value.ifBlank { stringResource(InstanceRes.string.anyType) }) },
            backgroundColor = Theme[colors][surface2],
            modifier = Modifier.width(150.dp)
        )

        AppSelectField(
            value = state.loaderFilter,
            items = state.loaderList,
            onValueChange = { store.intent(InstanceListIntent.OnLoaderFilterChange(it)) },
            itemRenderer = { value, _ -> AppSelectItem(value.ifBlank { stringResource(InstanceRes.string.anyLoader) }) },
            backgroundColor = Theme[colors][surface2],
            modifier = Modifier.width(150.dp)
        )

        AppSelectField(
            value = state.versionFilter,
            items = state.versionList,
            onValueChange = { store.intent(InstanceListIntent.OnVersionFilterChange(it)) },
            itemRenderer = { value, _ -> AppSelectItem(value.ifBlank { stringResource(InstanceRes.string.anyVersion) }) },
            backgroundColor = Theme[colors][surface2],
            modifier = Modifier.width(150.dp)
        )

        AppTextField(
            placeholder = { AppText(stringResource(CoreRes.string.search)) },
            initialValue = state.nameFilter,
            onValueChange = { store.intent(InstanceListIntent.OnNameFilterChange(it)) },
            imeAction = ImeAction.Search,
            backgroundColor = Theme[colors][surface2],
            modifier = Modifier.weight(1f)
        )
    }
}
