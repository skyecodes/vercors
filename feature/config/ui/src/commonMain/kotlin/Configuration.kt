package app.vercors.feature.config.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.vercors.core.ui.component.*
import app.vercors.core.ui.modifier.appCard
import app.vercors.core.ui.theme.*
import app.vercors.shared.domain.model.JavaInstallation
import app.vercors.shared.ui.EventSink
import app.vercors.shared.ui.Store
import com.composables.icons.lucide.*
import com.composeunstyled.theme.Theme
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.zacsweers.metro.AppScope
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberDirectoryPickerLauncher
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.path
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@CircuitInject(ConfigurationScreen::class, AppScope::class)
@Composable
fun Configuration(
    store: Store<ConfigurationState, ConfigurationEvent>,
    modifier: Modifier = Modifier,
) {
    val (state, eventSink) = store
    with(eventSink) {
        Column(
            modifier = modifier.padding(horizontal = 8.dp)
                .fillMaxWidth()
                .scrollable(state = rememberScrollState(), orientation = Orientation.Horizontal),
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                item("general") {
                    GeneralConfigurationCategory(
                        state = state
                    )
                }
                item("ui") {
                    UIConfigurationCategory(
                        state = state
                    )
                }
                item("java") {
                    JavaConfigurationCategory(
                        state = state
                    )
                }
                if (state.showNativeLibraryOptions) {
                    item("nativeLibraries") {
                        NativeLibraryConfigurationCategory(
                            state = state
                        )
                    }
                }
            }
        }
    }
}

@Composable
context(eventSink: EventSink<ConfigurationEvent>)
private fun GeneralConfigurationCategory(
    state: ConfigurationState
) {
    ConfigurationCategory(
        icon = Lucide.Settings,
        titleRes = ConfigRes.string.general,
    ) {
        if (state.platformList.isNotEmpty()) {
            ConfigurationRow(
                title = ConfigRes.string.preferredPlatform,
                description = ConfigRes.string.preferredPlatformDesc,
            ) {
                AppSelectField(
                    value = state.platformList.find { it.id == state.preferredPlatform } ?: state.platformList.first(),
                    items = state.platformList,
                    onValueChange = { eventSink(ConfigurationEvent.SetPreferredPlatform(it.id)) },
                    itemRenderer = { platform, _ ->
                        AppText(text = platform.name)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        ConfigurationRow(
            title = ConfigRes.string.metaApiUrl,
            description = ConfigRes.string.metaApiUrlDesc,
        ) {
            AppTextField(
                initialValue = state.metaApiUrl.orEmpty(),
                onValueChange = { eventSink(ConfigurationEvent.SetMetaApiUrl(it)) },
                debounceMillis = 500,
                backgroundColor = Theme[colors][surface2],
                modifier = Modifier.weight(1f)
            )
        }
        if (state.parallelDownloads != null) {
            ConfigurationRow(
                title = ConfigRes.string.parallelDownloads,
                description = ConfigRes.string.parallelDownloadsDesc,
            ) {
                AppNumberField(
                    value = state.parallelDownloads,
                    onValueChange = { eventSink(ConfigurationEvent.SetParallelDownloads(it)) },
                    valueRange = 1..64,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        if (state.parallelFilesystemOperations != null) {
            ConfigurationRow(
                title = ConfigRes.string.parallelFilesystemOperations,
                description = ConfigRes.string.parallelFilesystemOperationsDesc,
            ) {
                AppNumberField(
                    value = state.parallelFilesystemOperations,
                    onValueChange = { eventSink(ConfigurationEvent.SetParallelFilesystemOperations(it)) },
                    valueRange = 1..64,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
context(eventSink: EventSink<ConfigurationEvent>)
private fun UIConfigurationCategory(
    state: ConfigurationState
) {
    ConfigurationCategory(
        icon = Lucide.Eye,
        titleRes = ConfigRes.string.ui,
    ) {
        if (state.selectedTheme != null) {
            ConfigurationRow(
                title = ConfigRes.string.theme,
                description = ConfigRes.string.themeDesc,
            ) {
                AppSelectField(
                    value = state.selectedTheme,
                    items = state.themeList,
                    onValueChange = { eventSink(ConfigurationEvent.SelectTheme(it.id)) },
                    itemRenderer = { theme, _ ->
                        AppText(text = theme.displayName)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
            if (state.selectedAccent != null) {
                ConfigurationRow(
                    title = ConfigRes.string.accent,
                    description = ConfigRes.string.accentDesc,
                ) {
                    AppSelectField(
                        value = state.selectedAccent,
                        items = state.selectedTheme.accents,
                        onValueChange = { eventSink(ConfigurationEvent.SelectAccent(it.id)) },
                        itemRenderer = { accent, _ ->
                            Box(modifier = Modifier.size(16.dp).background(accent.color))
                            AppText(text = accent.name)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        if (state.animations != null) {
            ConfigurationRow(
                title = ConfigRes.string.animations,
                description = ConfigRes.string.animationsDesc,
            ) {
                AppCheckbox(
                    checked = state.animations,
                    onCheckedChange = { eventSink(ConfigurationEvent.ToggleAnimations) }
                )
            }
        }
        if (state.uiScalePercent != null) {
            ConfigurationRow(
                title = ConfigRes.string.uiScale,
                description = ConfigRes.string.uiScaleDesc,
            ) {
                AppSelectField(
                    value = state.uiScalePercent,
                    items = UI_SCALE_OPTIONS,
                    onValueChange = { eventSink(ConfigurationEvent.SetUiScalePercent(it)) },
                    itemRenderer = { percent, _ -> AppSelectItem(text = "$percent%") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
context(eventSink: EventSink<ConfigurationEvent>)
private fun JavaConfigurationCategory(
    state: ConfigurationState
) {
    ConfigurationCategory(
        icon = Lucide.Coffee,
        titleRes = ConfigRes.string.java,
    ) {


        val pickerLauncher = rememberDirectoryPickerLauncher { directory ->
            directory?.let { eventSink(ConfigurationEvent.AddJavaInstallation(it.path)) }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                AppText(text = stringResource(ConfigRes.string.javaInstallations), fontWeight = FontWeight.SemiBold)
                AppText(text = stringResource(ConfigRes.string.javaInstallationsDesc), style = Theme[textStyles][label])
            }
            AppIconTextButton(
                onClick = pickerLauncher::launch,
                text = stringResource(ConfigRes.string.addInstallation),
                imageVector = Lucide.Plus,
            )
            AppIconTextButton(
                onClick = { eventSink(ConfigurationEvent.RescanJavaInstallations) },
                text = stringResource(if (state.javaScanInProgress) ConfigRes.string.rescanning else ConfigRes.string.rescan),
                imageVector = Lucide.RefreshCw,
                enabled = !state.javaScanInProgress,
            )
        }

        state.javaInstallations.groupBy { it.majorVersion }.toSortedMap().forEach { (majorVersion, installations) ->
            JavaVersionGroup(majorVersion, installations, state.preferredJavaByMajorVersion[majorVersion])
        }

        state.javaAddError?.let { path ->
            AppText(
                text = stringResource(ConfigRes.string.javaAddFailed, path),
                color = Theme[colors][error],
                modifier = Modifier.padding(horizontal = 8.dp)
                    .clickable { eventSink(ConfigurationEvent.DismissJavaAddError) }
            )
        }

        val defaultMax = defaultMaxMemoryMb(state.totalMemoryMb)

        ConfigurationRow(title = ConfigRes.string.maxMemory, description = ConfigRes.string.maxMemoryDesc) {
            MemorySliderControl(
                value = state.jvmMemoryOptions.maxMemoryMb,
                defaultValue = defaultMax,
                valueRange = 512..state.totalMemoryMb,
                stepSize = 128,
                onCheckedChange = { checked -> eventSink(ConfigurationEvent.SetMaxMemoryMb(if (checked) defaultMax else null)) },
                onValueChange = { eventSink(ConfigurationEvent.SetMaxMemoryMb(it)) },
            )
        }
        ConfigurationRow(title = ConfigRes.string.minMemory, description = ConfigRes.string.minMemoryDesc) {
            MemorySliderControl(
                value = state.jvmMemoryOptions.minMemoryMb,
                defaultValue = defaultMinMemoryMb(),
                valueRange = 256..(state.jvmMemoryOptions.maxMemoryMb ?: defaultMax),
                stepSize = 32,
                onCheckedChange = { checked -> eventSink(ConfigurationEvent.SetMinMemoryMb(if (checked) defaultMinMemoryMb() else null)) },
                onValueChange = { eventSink(ConfigurationEvent.SetMinMemoryMb(it)) },
            )
        }
        ConfigurationRow(title = ConfigRes.string.extraJvmArgs, description = ConfigRes.string.extraJvmArgsDesc) {
            AppTextField(
                initialValue = state.jvmMemoryOptions.extraArgs.orEmpty(),
                onValueChange = { eventSink(ConfigurationEvent.SetExtraJvmArgs(it)) },
                debounceMillis = 500,
                backgroundColor = Theme[colors][surface2],
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
context(eventSink: EventSink<ConfigurationEvent>)
private fun NativeLibraryConfigurationCategory(
    state: ConfigurationState
) {
    ConfigurationCategory(
        icon = Lucide.Package,
        titleRes = ConfigRes.string.nativeLibraries,
    ) {
        ConfigurationRow(title = ConfigRes.string.useSystemGlfw, description = ConfigRes.string.useSystemGlfwDesc) {
            LibraryOverrideRow(
                checked = state.nativeLibraryOptions.useSystemGlfw == true,
                path = state.nativeLibraryOptions.glfwPath.orEmpty(),
                detectedPath = state.detectedGlfwPath,
                onCheckedChange = { eventSink(ConfigurationEvent.ToggleUseSystemGlfw(it)) },
                onPathChange = { eventSink(ConfigurationEvent.SetGlfwPath(it)) },
            )
        }
        ConfigurationRow(title = ConfigRes.string.useSystemOpenAl, description = ConfigRes.string.useSystemOpenAlDesc) {
            LibraryOverrideRow(
                checked = state.nativeLibraryOptions.useSystemOpenAl == true,
                path = state.nativeLibraryOptions.openAlPath.orEmpty(),
                detectedPath = state.detectedOpenAlPath,
                onCheckedChange = { eventSink(ConfigurationEvent.ToggleUseSystemOpenAl(it)) },
                onPathChange = { eventSink(ConfigurationEvent.SetOpenAlPath(it)) },
            )
        }
    }
}

@Composable
private fun RowScope.LibraryOverrideRow(
    checked: Boolean,
    path: String,
    detectedPath: String?,
    onCheckedChange: (Boolean) -> Unit,
    onPathChange: (String) -> Unit,
) {
    val pickerLauncher = rememberFilePickerLauncher(type = FileKitType.File("so")) { file ->
        file?.let { onPathChange(it.path) }
    }
    Row(
        modifier = Modifier.weight(1f),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppCheckbox(checked = checked, onCheckedChange = onCheckedChange)
        AppTextField(
            initialValue = path,
            onValueChange = onPathChange,
            debounceMillis = 500,
            enabled = checked,
            placeholder = { AppText(text = detectedPath ?: stringResource(ConfigRes.string.useDefaultPath)) },
            backgroundColor = Theme[colors][surface2],
            modifier = Modifier.weight(1f)
        )
        AppIconTextButton(
            onClick = pickerLauncher::launch,
            text = stringResource(ConfigRes.string.browse),
            imageVector = Lucide.FolderOpen,
            enabled = checked,
        )
    }
}

@Composable
private fun RowScope.MemorySliderControl(
    value: Int?,
    defaultValue: Int,
    valueRange: IntRange,
    stepSize: Int,
    onCheckedChange: (Boolean) -> Unit,
    onValueChange: (Int) -> Unit,
) {
    val checked = value != null
    val displayValue = (value ?: defaultValue).coerceIn(valueRange)
    Row(
        modifier = Modifier.weight(1f),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppCheckbox(checked = checked, onCheckedChange = onCheckedChange)
        AppSlider(
            value = displayValue,
            onValueChange = onValueChange,
            valueRange = valueRange,
            stepSize = stepSize,
            enabled = checked,
            modifier = Modifier.weight(1f)
        )
        AppNumberField(
            value = displayValue,
            onValueChange = onValueChange,
            valueRange = valueRange,
            enabled = checked,
        )
        AppText(text = stringResource(ConfigRes.string.megabytes), style = Theme[textStyles][label])
    }
}

private val UI_SCALE_OPTIONS = listOf(70, 80, 90, 100, 110, 125, 150, 175, 200)

private fun defaultMaxMemoryMb(totalMemoryMb: Int): Int = (((totalMemoryMb / 4).coerceIn(1024, 4096)) / 128) * 128
private fun defaultMinMemoryMb(): Int = 512

@Composable
context(eventSink: EventSink<ConfigurationEvent>)
private fun JavaVersionGroup(majorVersion: Int, installations: List<JavaInstallation>, preferredId: String?) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIcon(
                imageVector = Lucide.Dot,
                contentDescription = null
            )
            AppText(
                text = stringResource(ConfigRes.string.javaMajorVersion, majorVersion),
                fontWeight = FontWeight.SemiBold
            )
        }
        AppRadioGroup(
            value = preferredId,
            onValueChange = { id -> eventSink(ConfigurationEvent.SetPreferredJava(majorVersion, id)) },
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                installations.forEach { installation ->
                    JavaInstallationRow(installation = installation, isSelected = installation.id == preferredId)
                }
            }
        }
    }
}

@Composable
private fun AppRadioGroupScope.JavaInstallationRow(installation: JavaInstallation, isSelected: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            AppText(
                text = "${installation.versionString} (${installation.vendor}, ${installation.arch})",
                fontSize = 12.sp
            )
            AppText(
                text = installation.path,
                style = Theme[textStyles][label],
                color = Theme[colors][onBackground].copy(alpha = 0.6f)
            )
        }
        AppRadioButton(
            value = installation.id,
            label = if (isSelected) stringResource(ConfigRes.string.preferred) else null,
        )
    }
}

@Composable
private fun ConfigurationCategory(
    titleRes: StringResource,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppIcon(
                imageVector = icon,
                contentDescription = null
            )
            AppText(
                text = stringResource(titleRes),
                style = Theme[textStyles][title]
            )
        }

        Column(
            modifier = Modifier.appCard().padding(8.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun ConfigurationRow(
    title: StringResource,
    description: StringResource,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            AppText(text = stringResource(title), fontWeight = FontWeight.SemiBold)
            AppText(text = stringResource(description), style = Theme[textStyles][label])
        }
        content()
    }
}
