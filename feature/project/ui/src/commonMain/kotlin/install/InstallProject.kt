package app.vercors.feature.project.ui.install

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.vercors.core.ui.animation.AppAnimatedVisibility
import app.vercors.core.ui.component.*
import app.vercors.core.ui.layout.AppDialog
import app.vercors.core.ui.layout.AppForm
import app.vercors.core.ui.modifier.clickableText
import app.vercors.core.ui.theme.*
import app.vercors.feature.game.ui.GameVersionSelector
import app.vercors.feature.game.ui.LoaderSelector
import app.vercors.feature.game.ui.LoaderVersionSelector
import app.vercors.feature.instance.domain.model.ManualDownloadItem
import app.vercors.feature.project.ui.*
import app.vercors.shared.ui.*
import com.composables.icons.lucide.*
import com.composeunstyled.theme.Theme
import opensavvy.state.progressive.valueOrNull
import org.jetbrains.compose.resources.stringResource

@Composable
fun InstallProject(
    store: Store<InstallProjectState, InstallProjectEvent>,
    modifier: Modifier = Modifier,
) {
    val (state, eventSink) = store

    AppDialog(
        modifier = modifier,
        onClickOutside = {
            eventSink(
                if (state.completedInstanceId != null) InstallProjectEvent.OnCloseClick
                else InstallProjectEvent.OnCancelClick
            )
        }
    ) {
        with(eventSink) {
            AppForm(modifier = Modifier.width(IntrinsicSize.Max)) {
                AppText(
                    text = stringResource(ProjectRes.string.installToInstance),
                    style = Theme[textStyles][title]
                )

                InstallProjectInstanceSelector(state)
                InstallProjectNewInstanceForm(state)
                InstallProjectFileSelector(state)
                InstallProjectDependenciesSection(state)
                InstallProjectManualDownloadsSection(state)
                if (state.installing || state.installProgress.fraction > 0f) {
                    AppDownloadProgress(
                        progress = state.installProgress.fraction,
                        download = state.installProgress.download,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                InstallProjectActionButtons(state)
            }
        }
    }
}

@Composable
context(eventSink: EventSink<InstallProjectEvent>)
private fun InstallProjectInstanceSelector(state: InstallProjectState) {
    val createNewInstanceLabel = stringResource(
        ProjectRes.string.createNewInstanceWithProject,
        state.projectType.displayName.lowercase()
    )

    AppSelectField(
        label = stringResource(ProjectRes.string.instance),
        value = state.selectedInstanceItem,
        items = state.instanceSelectorItems,
        onValueChange = { eventSink(InstallProjectEvent.OnInstanceSelectorItemSelected(it)) },
        itemRenderer = { value, _ ->
            when (value) {
                is InstanceSelectorItem.Existing -> AppSelectItem(text = value.instance.name)
                InstanceSelectorItem.CreateNew -> AppSelectItem(text = createNewInstanceLabel, icon = Lucide.Plus)
                null -> AppSelectItem(text = "")
            }
        },
        modifier = Modifier.width(400.dp),
    )
}

@Composable
context(eventSink: EventSink<InstallProjectEvent>)
private fun InstallProjectNewInstanceForm(state: InstallProjectState) {
    if (!state.creatingNewInstance) return

    AppTextField(
        label = stringResource(ProjectRes.string.instanceName),
        initialValue = state.newInstanceName,
        onValueChange = { eventSink(InstallProjectEvent.OnNewInstanceNameChange(it)) },
        modifier = Modifier.width(300.dp),
    )

    GameVersionSelector(
        selectedVersion = state.selectedNewGameVersion,
        versionList = state.newInstanceGameVersionList,
        onVersionChange = { eventSink(InstallProjectEvent.OnNewGameVersionChange(it)) },
    )

    LoaderSelector(
        selectedLoader = state.selectedNewLoader,
        availableLoaders = state.newInstanceLoaderList,
        onLoaderChange = { eventSink(InstallProjectEvent.OnNewLoaderChange(it)) },
        includeVanillaOption = !state.projectType.requiresLoader,
    )

    if (state.selectedNewLoader != null) {
        LoaderVersionSelector(
            selectedVersion = state.selectedNewLoaderVersion,
            versionList = state.newInstanceLoaderVersionList.valueOrNull ?: emptyList(),
            onVersionChange = { eventSink(InstallProjectEvent.OnNewLoaderVersionChange(it)) },
        )
    }
}

@Composable
context(eventSink: EventSink<InstallProjectEvent>)
private fun InstallProjectFileSelector(state: InstallProjectState) {
    val targetResolved = state.selectedInstance != null ||
            (state.creatingNewInstance && state.selectedNewGameVersion != null)
    if (!targetResolved) return

    AppSelectField(
        label = stringResource(ProjectRes.string.`file`),
        value = state.selectedFile,
        items = state.fileList,
        onValueChange = { eventSink(InstallProjectEvent.OnFileSelected(it?.id)) },
        itemRenderer = { value, _ -> AppSelectItem(text = value?.displayName ?: "") },
        modifier = Modifier.width(400.dp),
    )
}

@Composable
context(eventSink: EventSink<InstallProjectEvent>)
private fun ColumnScope.InstallProjectDependenciesSection(state: InstallProjectState) {
    AppAnimatedVisibility(visible = state.dependencies.isNotEmpty()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AppText(
                text = stringResource(ProjectRes.string.requiredDependencies),
                fontWeight = FontWeight.Medium
            )
            state.dependencies.forEach { dependency ->
                AppCheckbox(
                    label = dependency.name,
                    checked = dependency.projectId in state.selectedDependencyIds,
                    onCheckedChange = {
                        eventSink(InstallProjectEvent.OnDependencyToggled(dependency.projectId, it))
                    },
                    invert = true
                )
            }
        }
    }
}

@Composable
context(eventSink: EventSink<InstallProjectEvent>)
private fun InstallProjectManualDownloadsSection(state: InstallProjectState) {
    if (state.manualDownloadItems.isEmpty()) return

    val uriHandler = LocalUriHandler.current
    AppText(text = stringResource(ProjectRes.string.manualDownloadsRequired))
    state.manualDownloadItems.forEach { item ->
        InstallProjectManualDownloadRow(item)
    }
    AppIconTextButton(
        onClick = { state.manualDownloadItems.forEach { uriHandler.openUri(it.downloadUrl) } },
        text = stringResource(ProjectRes.string.openAll),
        imageVector = Lucide.ExternalLink,
    )
}

@Composable
context(eventSink: EventSink<InstallProjectEvent>)
private fun InstallProjectManualDownloadRow(item: ManualDownloadItem) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppText(text = item.fileName, modifier = Modifier.weight(1f))
        if (item.downloaded) {
            AppIcon(
                imageVector = Lucide.Check,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        } else {
            AppIcon(
                imageVector = Lucide.ClipboardCopy,
                contentDescription = stringResource(SharedRes.string.copy),
                modifier = Modifier.size(16.dp)
                    .clickableText { eventSink(InstallProjectEvent.OnCopyUrlClick(item.downloadUrl)) }
            )
        }
    }
}

@Composable
context(eventSink: EventSink<InstallProjectEvent>)
private fun InstallProjectActionButtons(state: InstallProjectState) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (state.completedInstanceId != null) {
            AppIconTextButton(
                onClick = { eventSink(InstallProjectEvent.OnCloseClick) },
                text = stringResource(ProjectRes.string.close),
                imageVector = Lucide.X,
            )
            AppIconTextButton(
                onClick = { eventSink(InstallProjectEvent.OnGoToInstanceClick) },
                text = stringResource(ProjectRes.string.goToInstance),
                imageVector = Lucide.ArrowRight,
                backgroundColor = Theme[colors][primary],
                contentColor = Theme[colors][onPrimary],
            )
        } else {
            AppIconTextButton(
                onClick = { eventSink(InstallProjectEvent.OnCancelClick) },
                text = stringResource(SharedRes.string.cancel),
                imageVector = Lucide.X,
            )
            AppIconTextButton(
                onClick = { eventSink(InstallProjectEvent.OnConfirmClick) },
                text = stringResource(ProjectRes.string.install),
                imageVector = Lucide.Download,
                backgroundColor = Theme[colors][primary],
                contentColor = Theme[colors][onPrimary],
                enabled = state.canInstall
            )
        }
    }
}
