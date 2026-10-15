package app.vercors.feature.config.ui.java

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.vercors.core.ui.component.*
import app.vercors.core.ui.theme.*
import app.vercors.feature.config.ui.*
import app.vercors.shared.domain.model.DEFAULT_MIN_MEMORY_MB
import app.vercors.shared.domain.model.JavaInstallation
import app.vercors.shared.domain.model.defaultMaxMemoryMb
import app.vercors.shared.ui.*
import app.vercors.shared.ui.component.MemorySliderInputs
import com.composables.icons.lucide.*
import com.composeunstyled.theme.Theme
import io.github.vinceglb.filekit.dialogs.compose.rememberDirectoryPickerLauncher
import io.github.vinceglb.filekit.path
import org.jetbrains.compose.resources.stringResource

@Composable
fun JavaConfiguration(
    content: Store<JavaConfigurationState, JavaConfigurationEvent>,
    horizontalTabs: Boolean?,
) {
    val (state, eventSink) = content
    with(eventSink) {
        JavaConfigurationContent(state, horizontalTabs)
    }
}

@Composable
context(eventSink: EventSink<JavaConfigurationEvent>)
private fun JavaConfigurationContent(state: JavaConfigurationState, horizontalTabs: Boolean?) {
    ConfigurationTabScaffold(horizontalTabs) {
        val pickerLauncher = rememberDirectoryPickerLauncher { directory ->
            directory?.let { eventSink(JavaConfigurationEvent.OnJavaInstallationSelected(it.path)) }
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
                onClick = { eventSink(JavaConfigurationEvent.OnRescanJavaClick) },
                text = stringResource(if (state.javaScanInProgress) ConfigRes.string.rescanning else ConfigRes.string.rescan),
                imageVector = Lucide.RefreshCw,
                enabled = !state.javaScanInProgress,
            )
        }

        state.javaInstallations.groupBy { it.majorVersion }.toSortedMap().forEach { (majorVersion, installations) ->
            JavaConfigurationVersionGroup(majorVersion, installations, state.preferredJavaByMajorVersion[majorVersion])
        }

        state.javaAddError?.let { path ->
            AppText(
                text = stringResource(ConfigRes.string.javaAddFailed, path),
                color = Theme[colors][error],
                modifier = Modifier.padding(horizontal = 8.dp)
                    .clickable { eventSink(JavaConfigurationEvent.OnJavaAddErrorDismiss) }
            )
        }

        val defaultMax = defaultMaxMemoryMb(state.totalMemoryMb)

        ConfigurationRow(title = SharedRes.string.maxMemory, description = SharedRes.string.maxMemoryDesc) {
            JavaConfigurationMemorySlider(
                value = state.jvmMemoryOptions.maxMemoryMb,
                defaultValue = defaultMax,
                valueRange = 512..state.totalMemoryMb,
                stepSize = 128,
                onCheckedChange = { checked -> eventSink(JavaConfigurationEvent.OnMaxMemoryMbChange(if (checked) defaultMax else null)) },
                onValueChange = { eventSink(JavaConfigurationEvent.OnMaxMemoryMbChange(it)) },
            )
        }
        ConfigurationRow(title = SharedRes.string.minMemory, description = SharedRes.string.minMemoryDesc) {
            JavaConfigurationMemorySlider(
                value = state.jvmMemoryOptions.minMemoryMb,
                defaultValue = DEFAULT_MIN_MEMORY_MB,
                valueRange = 256..(state.jvmMemoryOptions.maxMemoryMb ?: defaultMax),
                stepSize = 32,
                onCheckedChange = { checked -> eventSink(JavaConfigurationEvent.OnMinMemoryMbChange(if (checked) DEFAULT_MIN_MEMORY_MB else null)) },
                onValueChange = { eventSink(JavaConfigurationEvent.OnMinMemoryMbChange(it)) },
            )
        }
        ConfigurationRow(title = SharedRes.string.extraJvmArgs, description = SharedRes.string.extraJvmArgsDesc) {
            AppTextField(
                initialValue = state.jvmMemoryOptions.extraArgs.orEmpty(),
                onValueChange = { eventSink(JavaConfigurationEvent.OnExtraJvmArgsChange(it)) },
                debounceMillis = 500,
                backgroundColor = Theme[colors][surface2],
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun RowScope.JavaConfigurationMemorySlider(
    value: Int?,
    defaultValue: Int,
    valueRange: IntRange,
    stepSize: Int,
    onCheckedChange: (Boolean) -> Unit,
    onValueChange: (Int) -> Unit,
) {
    Row(
        modifier = Modifier.weight(1f),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MemorySliderInputs(
            checked = value != null,
            value = value ?: defaultValue,
            valueRange = valueRange,
            stepSize = stepSize,
            onCheckedChange = onCheckedChange,
            onValueChange = onValueChange,
        )
    }
}

@Composable
context(eventSink: EventSink<JavaConfigurationEvent>)
private fun JavaConfigurationVersionGroup(
    majorVersion: Int,
    installations: List<JavaInstallation>,
    preferredId: String?
) {
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
            onValueChange = { id -> eventSink(JavaConfigurationEvent.OnPreferredJavaChange(majorVersion, id)) },
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                installations.forEach { installation ->
                    JavaConfigurationInstallationRow(
                        installation = installation,
                        isSelected = installation.id == preferredId
                    )
                }
            }
        }
    }
}

@Composable
context(eventSink: EventSink<JavaConfigurationEvent>)
private fun AppRadioGroupScope.JavaConfigurationInstallationRow(installation: JavaInstallation, isSelected: Boolean) {
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
                style = Theme[textStyles][label],
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
        AppIconTextButton(
            text = stringResource(ConfigRes.string.forget),
            imageVector = Lucide.X,
            onClick = { eventSink(JavaConfigurationEvent.OnForgetInstallation(installation.id)) }
        )
    }
}
