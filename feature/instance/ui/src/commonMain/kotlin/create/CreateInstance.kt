package app.vercors.feature.instance.ui.create

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.vercors.core.ui.animation.AppAnimatedContent
import app.vercors.core.ui.animation.appAnimateColorAsState
import app.vercors.core.ui.component.AppIconTextButton
import app.vercors.core.ui.component.AppText
import app.vercors.core.ui.component.AppTextField
import app.vercors.core.ui.layout.AppDialog
import app.vercors.core.ui.layout.AppForm
import app.vercors.core.ui.layout.AppLoading
import app.vercors.core.ui.theme.*
import app.vercors.feature.game.ui.GameVersionSelector
import app.vercors.feature.game.ui.LoaderSelector
import app.vercors.feature.game.ui.LoaderVersionSelector
import app.vercors.feature.instance.ui.*
import app.vercors.shared.ui.*
import com.composables.icons.lucide.*
import com.composeunstyled.theme.Theme
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.path
import opensavvy.state.progressive.valueOrNull
import org.jetbrains.compose.resources.stringResource

@Composable
fun CreateInstance(
    store: Store<CreateInstanceState, CreateInstanceEvent>,
    modifier: Modifier = Modifier,
) {
    val (state, eventSink) = store

    with(eventSink) {
        AppDialog(
            modifier = modifier,
            onClickOutside = { eventSink(CreateInstanceEvent.OnCancelClick) }
        ) {
            AppForm {
                AppText(
                    text = stringResource(InstanceRes.string.createInstance),
                    style = Theme[textStyles][title]
                )

                InstanceTypeSelector(
                    selectedInstanceType = state.selectedInstanceType,
                )

                AppAnimatedContent(state.selectedInstanceType) {
                    AppForm {
                        when (it) {
                            InstanceType.Custom -> CustomInstanceForm(state)
                            InstanceType.Modpack -> ModpackInstanceForm()
                            InstanceType.Import -> ImportInstanceForm(state)
                        }
                    }
                }
            }
        }
    }
}

@Composable
context(_: EventSink<CreateInstanceEvent>)
private fun InstanceTypeSelector(
    selectedInstanceType: InstanceType,
) {
    Column(
        modifier = Modifier.width(IntrinsicSize.Max),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AppText(
            text = stringResource(InstanceRes.string.instanceType),
            fontWeight = FontWeight.Medium
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            InstanceTypeButton(
                selectedInstanceType = selectedInstanceType,
                value = InstanceType.Custom,
                text = stringResource(InstanceRes.string.custom),
                icon = Lucide.Settings,
            )
            InstanceTypeButton(
                selectedInstanceType = selectedInstanceType,
                value = InstanceType.Modpack,
                text = stringResource(SharedRes.string.modpack),
                icon = Lucide.PackageOpen,
            )
            InstanceTypeButton(
                selectedInstanceType = selectedInstanceType,
                value = InstanceType.Import,
                text = stringResource(InstanceRes.string.import),
                icon = Lucide.Import
            )
        }
    }
}

@Composable
context(eventSink: EventSink<CreateInstanceEvent>)
private fun InstanceTypeButton(
    selectedInstanceType: InstanceType,
    value: InstanceType,
    text: String,
    icon: ImageVector
) {
    val isSelected = selectedInstanceType == value
    val animatedBackgroundColor by appAnimateColorAsState(if (isSelected) Theme[colors][primary] else Theme[colors][surface2])
    val animatedContentColor by appAnimateColorAsState(if (isSelected) Theme[colors][onPrimary] else Theme[colors][onBackground])
    AppIconTextButton(
        onClick = { eventSink(CreateInstanceEvent.OnInstanceTypeChange(value)) },
        text = text,
        imageVector = icon,
        backgroundColor = animatedBackgroundColor,
        contentColor = animatedContentColor,
    )
}

@Composable
context(eventSink: EventSink<CreateInstanceEvent>)
private fun CustomInstanceForm(state: CreateInstanceState) {
    AppTextField(
        label = stringResource(SharedRes.string.instanceName),
        initialValue = state.instanceName,
        onValueChange = { eventSink(CreateInstanceEvent.OnInstanceNameChange(it)) },
        modifier = Modifier.width(300.dp),
    )

    GameVersionSelector(
        selectedVersion = state.selectedGameVersion,
        versionList = state.gameVersionList.valueOrNull ?: emptyList(),
        onVersionChange = { eventSink(CreateInstanceEvent.OnGameVersionChange(it)) },
    )

    LoaderSelector(
        selectedLoader = state.selectedLoader,
        availableLoaders = state.loaderList.valueOrNull ?: emptyList(),
        onLoaderChange = { eventSink(CreateInstanceEvent.OnLoaderChange(it)) },
    )

    if (state.selectedLoader != null) {
        LoaderVersionSelector(
            selectedVersion = state.selectedLoaderVersion,
            versionList = state.loaderVersionList.valueOrNull ?: emptyList(),
            onVersionChange = { eventSink(CreateInstanceEvent.OnLoaderVersionChange(it)) },
        )
    }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AppIconTextButton(
            onClick = { eventSink(CreateInstanceEvent.OnCancelClick) },
            text = stringResource(SharedRes.string.cancel),
            imageVector = Lucide.X,
        )
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
private fun ModpackInstanceForm() {
    AppText(stringResource(InstanceRes.string.modpackInstanceInfo))

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AppIconTextButton(
            onClick = { eventSink(CreateInstanceEvent.OnCancelClick) },
            text = stringResource(SharedRes.string.cancel),
            imageVector = Lucide.X,
        )
        AppIconTextButton(
            onClick = { eventSink(CreateInstanceEvent.OnOpenModpacksScreen) },
            text = stringResource(InstanceRes.string.openModpacksScreen),
            imageVector = Lucide.Search,
            backgroundColor = Theme[colors][primary],
            contentColor = Theme[colors][onPrimary]
        )
    }
}

@Composable
context(eventSink: EventSink<CreateInstanceEvent>)
private fun ImportInstanceForm(state: CreateInstanceState) {
    val importPickerLauncher = rememberFilePickerLauncher(type = FileKitType.File("zip", "mrpack")) { file ->
        file?.let { eventSink(CreateInstanceEvent.OnImportFileSelected(it.path)) }
    }

    AppText(stringResource(InstanceRes.string.importInstanceInfo))

    AppIconTextButton(
        onClick = importPickerLauncher::launch,
        text = stringResource(InstanceRes.string.chooseFile),
        imageVector = Lucide.FileUp,
    )

    if (state.importParsing) AppLoading()

    if (state.importPreview != null) {
        AppTextField(
            label = stringResource(SharedRes.string.instanceName),
            initialValue = state.importInstanceName,
            onValueChange = { eventSink(CreateInstanceEvent.OnImportInstanceNameChange(it)) },
            modifier = Modifier.width(300.dp),
        )
    }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AppIconTextButton(
            onClick = { eventSink(CreateInstanceEvent.OnCancelClick) },
            text = stringResource(SharedRes.string.cancel),
            imageVector = Lucide.X,
        )
        AppIconTextButton(
            onClick = { eventSink(CreateInstanceEvent.OnConfirmImportClick) },
            text = stringResource(InstanceRes.string.import),
            imageVector = Lucide.Import,
            backgroundColor = Theme[colors][primary],
            contentColor = Theme[colors][onPrimary],
            enabled = state.canConfirmImport,
        )
    }
}
