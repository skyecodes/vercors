package app.vercors.feature.instance.ui.export

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import app.vercors.core.domain.toFractionOrNull
import app.vercors.feature.instance.domain.model.export.InstanceExportFormat
import app.vercors.feature.instance.domain.usecase.ListExportableEntriesUseCase
import app.vercors.feature.instance.ui.InstanceRes
import app.vercors.feature.instance.ui.errorExportFailed
import app.vercors.feature.instance.ui.exportSucceeded
import app.vercors.feature.project.domain.model.pack.InstanceExportOptions
import app.vercors.feature.project.domain.usecase.ExportInstanceUseCase
import app.vercors.shared.ui.*
import arrow.core.raise.context.either
import com.slack.circuit.overlay.OverlayNavigator
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

private const val DEFAULT_VERSION_ID = "1.0.0"

@AssistedInject
class ExportInstancePresenter(
    @Assisted private val target: ExportInstanceTarget,
    @Assisted private val navigator: OverlayNavigator<ExportInstanceResult>,
    private val globalScope: CoroutineScope,
    private val listExportableEntries: ListExportableEntriesUseCase,
    private val exportInstance: ExportInstanceUseCase,
    private val snackbarService: SnackbarService,
) : Presenter<ExportInstanceState, ExportInstanceEvent> {
    @Composable
    override fun present(): Store<ExportInstanceState, ExportInstanceEvent> {
        val options = rememberExportOptionsState()
        val (entries, setEntries) = remember { mutableStateOf<List<ExportEntryView>?>(null) }
        val (showAdvancedOptions, setShowAdvancedOptions) = remember { mutableStateOf(false) }
        val run = rememberExportRunState()

        LaunchedEffect(Unit) {
            setEntries(listExportableEntries(target.id).map { it.toView() })
        }

        return ExportInstanceState(
            suggestedFileName = target.toSuggestedFileName(),
            format = options.format,
            entries = entries,
            showAdvancedOptions = showAdvancedOptions,
            referenceKnownFiles = options.referenceKnownFiles,
            versionId = options.versionId,
            summary = options.summary,
            exportProgress = run.progress,
        ) + { event ->
            handleEvent(
                event = event,
                options = options,
                entries = entries,
                setEntries = setEntries,
                setShowAdvancedOptions = setShowAdvancedOptions,
                run = run,
            )
        }
    }

    @Composable
    private fun rememberExportOptionsState(): ExportOptionsState {
        val (format, setFormat) = remember { mutableStateOf(InstanceExportFormat.Vercors) }
        val (referenceKnownFiles, setReferenceKnownFiles) = remember { mutableStateOf(true) }
        val (versionId, setVersionId) = remember { mutableStateOf(DEFAULT_VERSION_ID) }
        val (summary, setSummary) = remember { mutableStateOf("") }
        return ExportOptionsState(
            format = format,
            setFormat = setFormat,
            referenceKnownFiles = referenceKnownFiles,
            setReferenceKnownFiles = setReferenceKnownFiles,
            versionId = versionId,
            setVersionId = setVersionId,
            summary = summary,
            setSummary = setSummary,
        )
    }

    @Composable
    private fun rememberExportRunState(): ExportRunState {
        val (progress, setProgress) = remember { mutableStateOf<Float?>(null) }
        val (job, setJob) = remember { mutableStateOf<Job?>(null) }
        return ExportRunState(
            progress = progress,
            setProgress = setProgress,
            job = job,
            setJob = setJob,
        )
    }

    private fun handleEvent(
        event: ExportInstanceEvent,
        options: ExportOptionsState,
        entries: List<ExportEntryView>?,
        setEntries: (List<ExportEntryView>?) -> Unit,
        setShowAdvancedOptions: (Boolean) -> Unit,
        run: ExportRunState,
    ) {
        when (event) {
            is ExportInstanceEvent.OnFormatSelected -> options.setFormat(event.format)
            is ExportInstanceEvent.OnAdvancedOptionsToggle ->
                handleAdvancedOptionsToggle(event.checked, entries, setEntries, setShowAdvancedOptions)

            is ExportInstanceEvent.OnEntryToggle -> setEntries(
                entries?.map { if (it.name == event.name) it.copy(checked = event.checked) else it }
            )

            is ExportInstanceEvent.OnReferenceKnownFilesToggle -> options.setReferenceKnownFiles(event.checked)
            is ExportInstanceEvent.OnVersionIdChange -> options.setVersionId(event.versionId)
            is ExportInstanceEvent.OnSummaryChange -> options.setSummary(event.summary)
            is ExportInstanceEvent.OnTargetSelected -> startExport(event.path, options, entries.orEmpty(), run)
            ExportInstanceEvent.OnCancelClick -> handleCancel(run)
        }
    }

    // Hidden selections would silently apply, so leaving advanced options restores the defaults
    private fun handleAdvancedOptionsToggle(
        checked: Boolean,
        entries: List<ExportEntryView>?,
        setEntries: (List<ExportEntryView>?) -> Unit,
        setShowAdvancedOptions: (Boolean) -> Unit,
    ) {
        setShowAdvancedOptions(checked)
        if (!checked) setEntries(entries?.map { it.copy(checked = it.includedByDefault) })
    }

    private fun handleCancel(run: ExportRunState) {
        val job = run.job
        if (job != null && job.isActive) {
            job.cancel()
            run.setJob(null)
            run.setProgress(null)
        } else {
            navigator.finish(ExportInstanceResult.Cancelled)
        }
    }

    private fun startExport(
        path: String,
        options: ExportOptionsState,
        entries: List<ExportEntryView>,
        run: ExportRunState,
    ) {
        val extension = options.format.fileExtension
        val targetPath = if (path.endsWith(".$extension", ignoreCase = true)) path else "$path.$extension"
        val selectedEntries = entries.filter { it.checked }.mapTo(HashSet()) { it.name }
        val exportOptions = InstanceExportOptions(
            referenceKnownFiles = options.referenceKnownFiles,
            versionId = options.versionId.trim(),
            summary = options.summary.trim(),
        )
        run.setProgress(0f)
        run.setJob(globalScope.launch {
            either {
                exportInstance(target.id, options.format, selectedEntries, exportOptions, targetPath) { progress ->
                    run.setProgress(progress.toFractionOrNull() ?: 0f)
                }
            }
                .onLeft {
                    run.setProgress(null)
                    snackbarService.error { stringResource(InstanceRes.string.errorExportFailed) }
                }
                .onRight {
                    snackbarService.success { stringResource(InstanceRes.string.exportSucceeded, target.name) }
                    navigator.finish(ExportInstanceResult.Exported)
                }
        })
    }

    @AssistedFactory
    fun interface Factory {
        fun create(
            target: ExportInstanceTarget,
            navigator: OverlayNavigator<ExportInstanceResult>
        ): ExportInstancePresenter
    }
}

private data class ExportOptionsState(
    val format: InstanceExportFormat,
    val setFormat: (InstanceExportFormat) -> Unit,
    val referenceKnownFiles: Boolean,
    val setReferenceKnownFiles: (Boolean) -> Unit,
    val versionId: String,
    val setVersionId: (String) -> Unit,
    val summary: String,
    val setSummary: (String) -> Unit,
)

private data class ExportRunState(
    val progress: Float?,
    val setProgress: (Float?) -> Unit,
    val job: Job?,
    val setJob: (Job?) -> Unit,
)
