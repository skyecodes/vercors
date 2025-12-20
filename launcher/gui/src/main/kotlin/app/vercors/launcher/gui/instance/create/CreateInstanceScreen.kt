package app.vercors.launcher.gui.instance.create

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.vercors.launcher.common.domain.instance.model.InstanceId
import app.vercors.launcher.gui.res.*
import app.vercors.shared.ui.component.*
import app.vercors.shared.ui.layout.AppForm
import app.vercors.shared.ui.modifier.appCard
import app.vercors.shared.ui.theme.*
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.composeunstyled.theme.Theme
import org.jetbrains.compose.resources.stringResource
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.compose.dsl.subscribe

@Composable
fun CreateInstanceScreen(
    container: CreateInstanceContainer,
    navigateToModpackList: () -> Unit,
    navigateToInstanceDetails: (InstanceId) -> Unit
) = with(container.store) {
    val state by subscribe { action ->
        when (action) {
            CreateInstanceAction.NavigateToModpackList -> navigateToModpackList()
            is CreateInstanceAction.NavigateToInstanceDetails -> navigateToInstanceDetails(action.id)
        }
    }
    CreateInstanceScreen(state)
}

@Composable
context(_: IntentReceiver<CreateInstanceIntent>)
private fun CreateInstanceScreen(state: CreateInstanceState) {
    Column(
        modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp).fillMaxSize(),
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

@Composable
context(store: IntentReceiver<CreateInstanceIntent>)
private fun InstanceTypeSelector(selectedTab: CreateInstanceTab) {
    AppText(
        text = stringResource(Res.string.createInstance),
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
                onClick = { store.intent(it.intent()) }
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
context(store: IntentReceiver<CreateInstanceIntent>)
private fun CreateCustomInstanceView(state: CreateInstanceState) {
    AppForm {
        AppTextField(
            label = stringResource(Res.string.instanceName),
            initialValue = state.instanceName,
            onValueChange = { store.intent(CreateInstanceIntent.OnInstanceNameChange(it)) },
            modifier = Modifier.width(300.dp),
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            AppSelectField(
                label = stringResource(Res.string.minecraftVersion),
                value = state.selectedGameVersion,
                items = state.filteredGameVersionList,
                onValueChange = { store.intent(CreateInstanceIntent.OnGameVersionChange(it)) },
                textConverter = { it?.displayName ?: "" },
                searchable = true,
                search = state.gameVersionFilter,
                onSearchChange = { store.intent(CreateInstanceIntent.OnGameVersionSearchChange(it)) },
                modifier = Modifier.width(300.dp),
            )

            AppCheckbox(
                label = stringResource(Res.string.showSnapshots),
                checked = state.showSnapshot,
                onCheckedChange = { store.intent(CreateInstanceIntent.OnShowSnapshotClick) }
            )
        }

        AppIconTextButton(
            onClick = { store.intent(CreateInstanceIntent.OnCreateInstanceClick) },
            text = stringResource(Res.string.create),
            imageVector = Lucide.Plus,
            backgroundColor = Theme[colors][primary],
            contentColor = Theme[colors][onPrimary],
            enabled = state.canCreateInstance,
        )
    }
}

@Composable
context(store: IntentReceiver<CreateInstanceIntent>)
private fun CreateModpackInstanceView(state: CreateInstanceState) {

}

@Composable
context(store: IntentReceiver<CreateInstanceIntent>)
private fun ImportLauncherInstanceView(state: CreateInstanceState) {

}
