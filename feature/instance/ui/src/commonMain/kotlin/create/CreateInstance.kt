package app.vercors.feature.instance.ui.create

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.vercors.core.ui.component.*
import app.vercors.core.ui.layout.AppForm
import app.vercors.core.ui.modifier.appCard
import app.vercors.core.ui.theme.*
import app.vercors.feature.game.ui.GameRes
import app.vercors.feature.game.ui.gameVersion
import app.vercors.feature.game.ui.showSnapshots
import app.vercors.feature.instance.ui.InstanceRes
import app.vercors.feature.instance.ui.createInstance
import app.vercors.feature.instance.ui.instanceName
import app.vercors.shared.ui.EventSink
import app.vercors.shared.ui.SharedRes
import app.vercors.shared.ui.Store
import app.vercors.shared.ui.create
import com.composables.icons.lucide.Bug
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.Star
import com.composeunstyled.theme.Theme
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.zacsweers.metro.AppScope
import opensavvy.state.progressive.valueOrNull
import org.jetbrains.compose.resources.stringResource

@CircuitInject(CreateInstanceScreen::class, AppScope::class)
@Composable
fun CreateInstance(
    store: Store<CreateInstanceState, CreateInstanceEvent>,
    modifier: Modifier = Modifier,
) {
    val (state, eventSink) = store

    with(eventSink) {
        Column(
            modifier = modifier.padding(vertical = 8.dp, horizontal = 16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            InstanceTypeSelector(state.selectedTab)

            AnimatedContent(
                targetState = state.selectedTab,
                modifier = Modifier.fillMaxSize()
            ) {
                when (it) {
                    CreateInstanceTab.Custom -> CreateCustomInstanceView(state)
                    CreateInstanceTab.Modpack -> CreateModpackInstanceView(state)
                    CreateInstanceTab.Import -> ImportLauncherInstanceView(state)
                }
            }
        }
    }
}

@Composable
context(eventSink: EventSink<CreateInstanceEvent>)
private fun InstanceTypeSelector(selectedTab: CreateInstanceTab) {
    AppText(
        text = stringResource(InstanceRes.string.createInstance),
        style = Theme[textStyles][title]
    )
    Row(
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CreateInstanceTab.entries.forEach {
            CreateInstanceTypeButton(
                isSelected = selectedTab == it,
                imageVector = it.icon,
                title = stringResource(it.title),
                description = stringResource(it.description),
                onClick = { eventSink(CreateInstanceEvent.OnTabClick(it)) }
            )
        }
    }
}

@Composable
private fun RowScope.CreateInstanceTypeButton(
    isSelected: Boolean,
    imageVector: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.appCard(
            onClick = onClick,
            backgroundColor = if (isSelected) Theme[colors][primary].copy(alpha = 0.2f) else Theme[colors][surface],
        ).weight(1f).fillMaxHeight().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AppIcon(
            imageVector = imageVector,
            contentDescription = title,
            modifier = Modifier.size(48.dp)
        )
        AppText(
            text = title,
            style = Theme[textStyles][subtitle],
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        AppText(
            text = description,
        )
    }
}

@Composable
context(eventSink: EventSink<CreateInstanceEvent>)
private fun CreateCustomInstanceView(state: CreateInstanceState) {
    if (state.gameVersionList.valueOrNull != null) LaunchedEffect(state.gameVersionList) {
        eventSink(CreateInstanceEvent.OnGameVersionChange(state.gameVersionList.valueOrNull?.firstOrNull { it.isLatestRelease }))
    }

    AppForm {
        AppTextField(
            label = stringResource(InstanceRes.string.instanceName),
            initialValue = state.instanceName,
            onValueChange = { eventSink(CreateInstanceEvent.OnInstanceNameChange(it)) },
            modifier = Modifier.width(300.dp),
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            AppSelectField(
                label = stringResource(GameRes.string.gameVersion),
                value = state.filteredSelectedGameVersion,
                items = state.filteredGameVersionList,
                onValueChange = { eventSink(CreateInstanceEvent.OnGameVersionChange(it)) },
                itemRenderer = { value, _ ->
                    AppSelectItem(
                        text = value?.displayName ?: "",
                        icon = if (value?.isLatestRelease == true) Lucide.Star else if (value?.isLatestSnapshot == true) Lucide.Bug else null
                    )
                },
                searchable = true,
                search = state.gameVersionFilter,
                onSearchChange = { eventSink(CreateInstanceEvent.OnGameVersionSearchChange(it)) },
                modifier = Modifier.width(300.dp),
            )

            AppCheckbox(
                modifier = Modifier.padding(bottom = 4.dp),
                label = stringResource(GameRes.string.showSnapshots),
                checked = state.showSnapshot,
                onCheckedChange = { eventSink(CreateInstanceEvent.OnShowSnapshotClick) }
            )
        }

        AppIconTextButton(
            onClick = { eventSink(CreateInstanceEvent.OnCreateInstanceClick) },
            text = stringResource(SharedRes.string.create),
            imageVector = Lucide.Plus,
            backgroundColor = Theme[colors][primary],
            contentColor = Theme[colors][onPrimary],
            enabled = state.canCreateInstance
        )
    }
}

@Composable
context(eventSink: EventSink<CreateInstanceEvent>)
private fun CreateModpackInstanceView(state: CreateInstanceState) {
    // TODO implement
}

@Composable
context(eventSink: EventSink<CreateInstanceEvent>)
private fun ImportLauncherInstanceView(state: CreateInstanceState) {
    // TODO implement
}
