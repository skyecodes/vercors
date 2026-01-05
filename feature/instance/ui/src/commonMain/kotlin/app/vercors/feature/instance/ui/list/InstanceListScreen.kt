package app.vercors.feature.instance.ui.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
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
import app.vercors.shared.ui.EventSink
import app.vercors.shared.ui.SharedRes
import app.vercors.shared.ui.Store
import app.vercors.shared.ui.create
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.composeunstyled.theme.Theme
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.zacsweers.metro.AppScope
import org.jetbrains.compose.resources.stringResource

@CircuitInject(InstanceListScreen::class, AppScope::class)
@Composable
fun InstanceList(
    store: Store<InstanceListState, InstanceListEvent>,
    modifier: Modifier = Modifier,
) {
    val (state, eventSink) = store

    with(eventSink) {
        Column(
            modifier = modifier.padding(8.dp),
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
                            onInstanceClick = { eventSink(InstanceListEvent.OnInstanceClick(it)) },
                            modifier = Modifier.fillMaxWidth().height(150.dp).appAnimateItem(),
                            origin = InstanceSharedElementOrigin.InstanceList
                        )
                    }
                }
            }
        }
    }
}

@Composable
context(eventSink: EventSink<InstanceListEvent>)
fun InstanceListFilterCard(state: InstanceListState) {
    Row(
        modifier = Modifier.appCard().padding(8.dp).fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIconTextButton(
            onClick = { eventSink(InstanceListEvent.OnCreateInstanceClick) },
            text = stringResource(SharedRes.string.create),
            imageVector = Lucide.Plus,
            backgroundColor = Theme[colors][primary],
            contentColor = Theme[colors][onPrimary]
        )

        AppSelectField(
            value = state.typeFilter,
            items = state.typeList,
            onValueChange = { eventSink(InstanceListEvent.OnTypeFilterChange(it)) },
            itemRenderer = { value, _ -> AppSelectItem(value.ifBlank { stringResource(InstanceRes.string.anyType) }) },
            backgroundColor = Theme[colors][surface2],
            modifier = Modifier.width(150.dp)
        )

        AppSelectField(
            value = state.loaderFilter,
            items = state.loaderList,
            onValueChange = { eventSink(InstanceListEvent.OnLoaderFilterChange(it)) },
            itemRenderer = { value, _ -> AppSelectItem(value.ifBlank { stringResource(InstanceRes.string.anyLoader) }) },
            backgroundColor = Theme[colors][surface2],
            modifier = Modifier.width(150.dp)
        )

        AppSelectField(
            value = state.versionFilter,
            items = state.versionList,
            onValueChange = { eventSink(InstanceListEvent.OnVersionFilterChange(it)) },
            itemRenderer = { value, _ -> AppSelectItem(value.ifBlank { stringResource(InstanceRes.string.anyVersion) }) },
            backgroundColor = Theme[colors][surface2],
            modifier = Modifier.width(150.dp)
        )

        AppTextField(
            placeholder = { AppText(stringResource(CoreRes.string.search)) },
            initialValue = state.nameFilter,
            onValueChange = { eventSink(InstanceListEvent.OnNameFilterChange(it)) },
            imeAction = ImeAction.Search,
            backgroundColor = Theme[colors][surface2],
            modifier = Modifier.weight(1f)
        )
    }
}
