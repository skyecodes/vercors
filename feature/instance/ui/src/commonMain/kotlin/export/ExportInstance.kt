package app.vercors.feature.instance.ui.export

import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.vercors.core.ui.animation.AppAnimatedVisibility
import app.vercors.core.ui.component.*
import app.vercors.core.ui.layout.AppDialog
import app.vercors.core.ui.layout.AppForm
import app.vercors.core.ui.theme.*
import app.vercors.feature.instance.domain.model.export.InstanceExportFormat
import app.vercors.feature.instance.ui.*
import app.vercors.shared.ui.EventSink
import app.vercors.shared.ui.SharedRes
import app.vercors.shared.ui.Store
import app.vercors.shared.ui.cancel
import com.composables.icons.lucide.*
import com.composeunstyled.theme.Theme
import org.jetbrains.compose.resources.stringResource

private val EXPORT_FORM_WIDTH = 400.dp
private val EXPORT_ENTRY_LIST_MAX_HEIGHT = 240.dp

@Composable
fun ExportInstance(
    store: Store<ExportInstanceState, ExportInstanceEvent>,
    modifier: Modifier = Modifier,
) {
    val (state, eventSink) = store
    val launchFileSaver = rememberExportFileSaver { path ->
        path?.let { eventSink(ExportInstanceEvent.OnTargetSelected(it)) }
    }

    AppDialog(
        modifier = modifier,
        onClickOutside = { if (!state.isExporting) eventSink(ExportInstanceEvent.OnCancelClick) }
    ) {
        with(eventSink) {
            AppForm(modifier = Modifier.width(EXPORT_FORM_WIDTH)) {
                AppText(
                    text = stringResource(InstanceRes.string.exportInstance),
                    style = Theme[textStyles][title]
                )

                ExportInstanceFormatSection(state)
                ExportInstanceAdvancedOptionsSection(state)

                state.exportProgress?.let { progress ->
                    AppProgressIndicator(progress = progress, modifier = Modifier.fillMaxWidth())
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppIconTextButton(
                        onClick = { eventSink(ExportInstanceEvent.OnCancelClick) },
                        text = stringResource(SharedRes.string.cancel),
                        imageVector = Lucide.X,
                    )
                    AppIconTextButton(
                        onClick = { launchFileSaver(state.suggestedFileName, state.format.fileExtension) },
                        text = stringResource(InstanceRes.string.exportAction),
                        imageVector = Lucide.Share,
                        backgroundColor = Theme[colors][primary],
                        contentColor = Theme[colors][onPrimary],
                        enabled = state.canExport
                    )
                }
            }
        }
    }
}

@Composable
context(eventSink: EventSink<ExportInstanceEvent>)
private fun ExportInstanceFormatSection(state: ExportInstanceState) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AppSelectField(
            label = stringResource(InstanceRes.string.exportFormat),
            value = state.format,
            items = InstanceExportFormat.entries,
            onValueChange = { eventSink(ExportInstanceEvent.OnFormatSelected(it)) },
            itemRenderer = { value, _ -> AppSelectItem(text = value.title) },
            modifier = Modifier.fillMaxWidth(),
        )
        AppText(text = state.format.description, style = Theme[textStyles][label])
    }

    when (state.format) {
        InstanceExportFormat.Vercors -> AppCheckbox(
            label = stringResource(InstanceRes.string.exportReferenceKnownFiles),
            checked = state.referenceKnownFiles,
            onCheckedChange = { eventSink(ExportInstanceEvent.OnReferenceKnownFilesToggle(it)) },
            invert = true,
        )

        InstanceExportFormat.Modrinth -> {
            AppTextField(
                label = stringResource(InstanceRes.string.exportVersionId),
                initialValue = state.versionId,
                onValueChange = { eventSink(ExportInstanceEvent.OnVersionIdChange(it)) },
                enabled = !state.isExporting,
                modifier = Modifier.fillMaxWidth(),
            )
            AppTextField(
                label = stringResource(InstanceRes.string.exportSummary),
                initialValue = state.summary,
                onValueChange = { eventSink(ExportInstanceEvent.OnSummaryChange(it)) },
                enabled = !state.isExporting,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        InstanceExportFormat.Prism -> Unit
    }
}

@Composable
context(eventSink: EventSink<ExportInstanceEvent>)
private fun ExportInstanceAdvancedOptionsSection(state: ExportInstanceState) {
    val entries = state.entries
    Column {
        AppCheckbox(
            label = stringResource(InstanceRes.string.exportAdvancedOptions),
            checked = state.showAdvancedOptions,
            onCheckedChange = { eventSink(ExportInstanceEvent.OnAdvancedOptionsToggle(it)) },
            invert = true,
        )
        AppAnimatedVisibility(
            visible = state.showAdvancedOptions,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            Column(
                modifier = Modifier.padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppText(
                    text = stringResource(
                        InstanceRes.string.exportIncludedFiles,
                        entries.orEmpty().count { it.checked },
                        entries.orEmpty().size
                    ),
                    fontWeight = FontWeight.Medium
                )
                if (entries == null) {
                    AppLoadingPlaceholder(modifier = Modifier.fillMaxWidth().height(24.dp))
                    return@Column
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = EXPORT_ENTRY_LIST_MAX_HEIGHT)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    entries.forEach { entry ->
                        AppCheckbox(
                            label = entry.name,
                            labelIcon = if (entry.isDirectory) Lucide.Folder else Lucide.File,
                            checked = entry.checked,
                            onCheckedChange = { eventSink(ExportInstanceEvent.OnEntryToggle(entry.name, it)) },
                            invert = true,
                        )
                    }
                }
            }
        }
    }
}

private val InstanceExportFormat.title: String
    @Composable get() = when (this) {
        InstanceExportFormat.Vercors -> stringResource(InstanceRes.string.exportFormatVercors)
        InstanceExportFormat.Prism -> stringResource(InstanceRes.string.exportFormatPrism)
        InstanceExportFormat.Modrinth -> stringResource(InstanceRes.string.exportFormatModrinth)
    }

private val InstanceExportFormat.description: String
    @Composable get() = when (this) {
        InstanceExportFormat.Vercors -> stringResource(InstanceRes.string.exportFormatVercorsDescription)
        InstanceExportFormat.Prism -> stringResource(InstanceRes.string.exportFormatPrismDescription)
        InstanceExportFormat.Modrinth -> stringResource(InstanceRes.string.exportFormatModrinthDescription)
    }
