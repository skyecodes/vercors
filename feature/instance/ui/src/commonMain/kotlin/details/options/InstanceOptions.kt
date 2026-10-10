package app.vercors.feature.instance.ui.details.options

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.vercors.core.ui.animation.AppAnimatedVisibility
import app.vercors.core.ui.component.*
import app.vercors.core.ui.modifier.appCard
import app.vercors.core.ui.theme.*
import app.vercors.feature.game.ui.*
import app.vercors.feature.instance.ui.*
import app.vercors.feature.instance.ui.details.InstanceView
import app.vercors.shared.domain.model.DEFAULT_MIN_MEMORY_MB
import app.vercors.shared.domain.model.JvmMemoryOptions
import app.vercors.shared.domain.model.NativeLibraryOptions
import app.vercors.shared.domain.model.defaultMaxMemoryMb
import app.vercors.shared.ui.*
import app.vercors.shared.ui.component.LibraryPathInputs
import app.vercors.shared.ui.component.MemorySliderInputs
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.RotateCcw
import com.composeunstyled.theme.Theme
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun InstanceOptions(
    instance: InstanceView,
    content: Store<InstanceOptionsState, InstanceOptionsEvent>,
    horizontalTabs: Boolean
) {
    with(content.eventSink) {
        InstanceOptionsScaffold(horizontalTabs) {
            InstanceGeneralOptionsSection(instance, content.state)
        }
    }
}

@Composable
fun InstanceJavaOptions(
    instance: InstanceView,
    content: Store<InstanceOptionsState, InstanceOptionsEvent>,
    horizontalTabs: Boolean
) {
    with(content.eventSink) {
        InstanceOptionsScaffold(horizontalTabs) {
            InstanceJavaOptionsSection(instance, content.state)
        }
    }
}

@Composable
fun InstanceAdvancedOptions(
    instance: InstanceView,
    content: Store<InstanceOptionsState, InstanceOptionsEvent>,
    horizontalTabs: Boolean
) {
    with(content.eventSink) {
        InstanceOptionsScaffold(horizontalTabs) {
            InstanceNativeLibraryOptionsSection(instance, content.state)
        }
    }
}

@Composable
private fun InstanceOptionsScaffold(
    horizontalTabs: Boolean,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .let { if (!horizontalTabs) it.padding(top = 8.dp) else it }
            .fillMaxSize()
            .scrollable(state = rememberScrollState(), orientation = Orientation.Horizontal),
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Column(
                    modifier = Modifier.appCard().padding(8.dp).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
context(eventSink: EventSink<InstanceOptionsEvent>)
private fun ColumnScope.InstanceGeneralOptionsSection(instance: InstanceView, state: InstanceOptionsState) {
    AppTextField(
        label = stringResource(SharedRes.string.instanceName) + if (state.isNameEdited) "*" else "",
        initialValue = state.editedInstance.name ?: instance.name,
        onValueChange = { eventSink(InstanceOptionsEvent.OnInstanceEditChange { state -> state.copy(name = it) }) },
        modifier = Modifier.widthIn(min = 300.dp)
    )

    var advancedOptionsUnlocked by remember(instance.id) { mutableStateOf(false) }

    if (instance.modpackInfo != null) {
        InstanceOptionsUnlockRow(
            checked = advancedOptionsUnlocked,
            onCheckedChange = { advancedOptionsUnlocked = it }
        )
    }

    AppAnimatedVisibility(
        visible = instance.modpackInfo == null || advancedOptionsUnlocked,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val gameVersion =
                remember(state.gameVersionList, state.editedInstance.gameVersion, instance.gameVersion) {
                    state.gameVersionList.find {
                        it.id == (state.editedInstance.gameVersion ?: instance.gameVersion)
                    }
                }

            GameVersionSelector(
                selectedVersion = gameVersion,
                versionList = state.gameVersionList,
                onVersionChange = {
                    it?.let {
                        eventSink(InstanceOptionsEvent.OnInstanceEditChange { state ->
                            state.copy(
                                gameVersion = it.id
                            )
                        })
                    }
                },
                label = stringResource(GameRes.string.gameVersion) + if (state.isGameVersionEdited) "*" else ""
            )

            InstanceGeneralOptionsLoader(state, instance)
        }
    }
}

@Composable
private fun InstanceOptionsUnlockRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        AppCheckbox(
            label = stringResource(InstanceRes.string.unlockAdvancedOptions),
            invert = true,
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        AppText(
            text = stringResource(InstanceRes.string.unlockAdvancedOptionsWarning),
            style = Theme[textStyles][label],
            color = Theme[colors][error]
        )
    }
}

@Composable
context(eventSink: EventSink<InstanceOptionsEvent>)
private fun InstanceGeneralOptionsLoader(
    state: InstanceOptionsState,
    instance: InstanceView
) {
    val loaderType = when (val edit = state.editedInstance.modLoaderType) {
        is ModLoaderTypeEdit.Loader -> edit.type
        ModLoaderTypeEdit.Vanilla -> null
        null -> instance.loaderType
    }

    LoaderSelector(
        selectedLoader = loaderType,
        availableLoaders = state.loaderList,
        onLoaderChange = { eventSink(InstanceOptionsEvent.OnLoaderChange(it)) },
        label = stringResource(GameRes.string.loader) + if (state.isLoaderTypeEdited) "*" else ""
    )

    if (loaderType != null) {
        val versionString = state.editedInstance.modLoaderVersion ?: instance.loaderVersion
        val selectedVersion = remember(state.loaderVersionList, versionString) {
            state.loaderVersionList.find { it.version == versionString }
        }

        LoaderVersionSelector(
            selectedVersion = selectedVersion,
            versionList = state.loaderVersionList,
            onVersionChange = { it?.let { eventSink(InstanceOptionsEvent.OnLoaderVersionChange(it.version)) } },
            label = stringResource(GameRes.string.loaderVersion) + if (state.isLoaderVersionEdited) "*" else ""
        )
    }
}

@Composable
context(eventSink: EventSink<InstanceOptionsEvent>)
private fun InstanceJavaOptionsSection(instance: InstanceView, state: InstanceOptionsState) {
    val selectedId = when (val edit = state.editedInstance.javaOverride) {
        is JavaOverrideEdit.Specific -> edit.installationId
        JavaOverrideEdit.UseDefault -> null
        null -> instance.javaInstallationId
    }
    val selected = state.availableJavaInstallations.find { it.id == selectedId }
    val inheritLabel = state.inheritedJavaInstallation?.path
        ?.let { stringResource(InstanceRes.string.useDefaultJavaResolved, it) }
        ?: stringResource(InstanceRes.string.useDefaultJava)
    val recommendedSuffix = state.requiredJavaMajorVersion
        ?.let { " " + stringResource(InstanceRes.string.recommendedJavaVersion, it) }
        .orEmpty()

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        AppSelectField(
            value = selected,
            items = listOf(null) + state.availableJavaInstallations,
            onValueChange = { eventSink(InstanceOptionsEvent.OnJavaOverrideChange(it?.id)) },
            label = stringResource(InstanceRes.string.javaOverride) +
                    (if (state.isJavaOverrideEdited) "*" else "") + recommendedSuffix,
            itemRenderer = { value, _ ->
                AppSelectItem(value?.path ?: inheritLabel)
            },
            modifier = Modifier.weight(1f),
        )
        InstanceOptionsResetButton(selectedId != null) {
            eventSink(InstanceOptionsEvent.OnJavaOverrideChange(null))
        }
    }

    val currentOptions = state.editedInstance.jvmMemoryOptionsEdit ?: instance.jvmMemoryOptions
    val effectiveOptions = currentOptions.withFallback(state.globalJvmMemoryOptions)
    val defaultMax = defaultMaxMemoryMb(state.totalMemoryMb)

    fun updateOptions(update: (JvmMemoryOptions) -> JvmMemoryOptions) {
        eventSink(InstanceOptionsEvent.OnInstanceEditChange { s ->
            s.copy(jvmMemoryOptionsEdit = update(currentOptions))
        })
    }

    InstanceMemorySliderRow(
        title = SharedRes.string.maxMemory,
        description = SharedRes.string.maxMemoryDesc,
        edited = state.isMaxMemoryEdited,
        checked = effectiveOptions.maxMemoryEnabled == true,
        value = effectiveOptions.maxMemoryMb ?: defaultMax,
        valueRange = 512..state.totalMemoryMb,
        stepSize = 128,
        isOverridden = currentOptions.maxMemoryEnabled != null || currentOptions.maxMemoryMb != null,
        onReset = { updateOptions { it.copy(maxMemoryEnabled = null, maxMemoryMb = null) } },
        onCheckedChange = { checked ->
            updateOptions { opts ->
                opts.copy(
                    maxMemoryEnabled = checked,
                    maxMemoryMb = if (checked) opts.maxMemoryMb ?: effectiveOptions.maxMemoryMb
                    ?: defaultMax else opts.maxMemoryMb,
                )
            }
        },
        onValueChange = { newValue ->
            updateOptions { opts ->
                val clampedMin = opts.minMemoryMb?.let { min -> minOf(min, newValue) }
                opts.copy(maxMemoryEnabled = true, maxMemoryMb = newValue, minMemoryMb = clampedMin)
            }
        },
    )
    InstanceMemorySliderRow(
        title = SharedRes.string.minMemory,
        description = SharedRes.string.minMemoryDesc,
        edited = state.isMinMemoryEdited,
        checked = effectiveOptions.minMemoryEnabled == true,
        value = effectiveOptions.minMemoryMb ?: DEFAULT_MIN_MEMORY_MB,
        valueRange = 256..(effectiveOptions.maxMemoryMb ?: defaultMax),
        stepSize = 32,
        isOverridden = currentOptions.minMemoryEnabled != null || currentOptions.minMemoryMb != null,
        onReset = { updateOptions { it.copy(minMemoryEnabled = null, minMemoryMb = null) } },
        onCheckedChange = { checked ->
            updateOptions { opts ->
                opts.copy(
                    minMemoryEnabled = checked,
                    minMemoryMb = if (checked) opts.minMemoryMb ?: effectiveOptions.minMemoryMb
                    ?: DEFAULT_MIN_MEMORY_MB else opts.minMemoryMb,
                )
            }
        },
        onValueChange = { newValue -> updateOptions { it.copy(minMemoryEnabled = true, minMemoryMb = newValue) } },
    )
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        AppText(
            text = stringResource(SharedRes.string.extraJvmArgs) + if (state.isExtraJvmArgsEdited) "*" else "",
            fontWeight = FontWeight.SemiBold
        )
        AppText(text = stringResource(SharedRes.string.extraJvmArgsDesc), style = Theme[textStyles][label])
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppTextField(
                initialValue = effectiveOptions.extraArgs.orEmpty(),
                onValueChange = { newValue -> updateOptions { it.copy(extraArgs = newValue) } },
                backgroundColor = Theme[colors][surface2],
                modifier = Modifier.weight(1f)
            )
            InstanceOptionsResetButton(currentOptions.extraArgs != null) {
                updateOptions { it.copy(extraArgs = null) }
            }
        }
    }
}

@Composable
private fun InstanceOptionsResetButton(enabled: Boolean, onClick: () -> Unit) {
    AppIconTextButton(
        onClick = onClick,
        text = stringResource(InstanceRes.string.reset),
        imageVector = Lucide.RotateCcw,
        enabled = enabled,
    )
}

@Composable
context(eventSink: EventSink<InstanceOptionsEvent>)
private fun InstanceNativeLibraryOptionsSection(instance: InstanceView, state: InstanceOptionsState) {
    val currentOptions = state.editedInstance.nativeLibraryOptionsEdit ?: instance.nativeLibraryOptions
    val effectiveOptions = currentOptions.withFallback(state.globalNativeLibraryOptions)

    fun updateOptions(update: (NativeLibraryOptions) -> NativeLibraryOptions) {
        eventSink(InstanceOptionsEvent.OnInstanceEditChange { s ->
            s.copy(nativeLibraryOptionsEdit = update(currentOptions))
        })
    }

    InstanceLibraryOverrideRow(
        title = SharedRes.string.useSystemGlfw,
        description = SharedRes.string.useSystemGlfwDesc,
        edited = state.isGlfwEdited,
        checked = effectiveOptions.useSystemGlfw == true,
        path = effectiveOptions.glfwPath.orEmpty(),
        detectedPath = state.detectedGlfwPath,
        isOverridden = currentOptions.useSystemGlfw != null || currentOptions.glfwPath != null,
        onReset = { updateOptions { it.copy(useSystemGlfw = null, glfwPath = null) } },
        onCheckedChange = { checked -> updateOptions { it.copy(useSystemGlfw = checked) } },
        onPathChange = { newValue -> updateOptions { it.copy(glfwPath = newValue) } },
    )
    InstanceLibraryOverrideRow(
        title = SharedRes.string.useSystemOpenAl,
        description = SharedRes.string.useSystemOpenAlDesc,
        edited = state.isOpenAlEdited,
        checked = effectiveOptions.useSystemOpenAl == true,
        path = effectiveOptions.openAlPath.orEmpty(),
        detectedPath = state.detectedOpenAlPath,
        isOverridden = currentOptions.useSystemOpenAl != null || currentOptions.openAlPath != null,
        onReset = { updateOptions { it.copy(useSystemOpenAl = null, openAlPath = null) } },
        onCheckedChange = { checked -> updateOptions { it.copy(useSystemOpenAl = checked) } },
        onPathChange = { newValue -> updateOptions { it.copy(openAlPath = newValue) } },
    )
}

@Composable
private fun InstanceLibraryOverrideRow(
    title: StringResource,
    description: StringResource,
    checked: Boolean,
    path: String,
    detectedPath: String?,
    isOverridden: Boolean,
    edited: Boolean = false,
    onReset: () -> Unit,
    onCheckedChange: (Boolean) -> Unit,
    onPathChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        AppText(
            text = stringResource(title) + if (edited) "*" else "",
            fontWeight = FontWeight.SemiBold
        )
        AppText(
            text = stringResource(description),
            style = Theme[textStyles][label]
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LibraryPathInputs(checked, path, detectedPath, onCheckedChange, onPathChange)
            InstanceOptionsResetButton(
                enabled = isOverridden,
                onClick = onReset
            )
        }
    }
}

@Composable
private fun InstanceMemorySliderRow(
    title: StringResource,
    description: StringResource,
    checked: Boolean,
    value: Int,
    valueRange: IntRange,
    stepSize: Int,
    isOverridden: Boolean,
    edited: Boolean = false,
    onReset: () -> Unit,
    onCheckedChange: (Boolean) -> Unit,
    onValueChange: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        AppText(text = stringResource(title) + if (edited) "*" else "", fontWeight = FontWeight.SemiBold)
        AppText(text = stringResource(description), style = Theme[textStyles][label])
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MemorySliderInputs(checked, value, valueRange, stepSize, onCheckedChange, onValueChange)
            InstanceOptionsResetButton(
                enabled = isOverridden,
                onClick = onReset
            )
        }
    }
}
