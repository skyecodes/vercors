package app.vercors.feature.instance.ui.options.copy

import androidx.compose.runtime.*
import app.vercors.core.ui.state.collectProgressiveAsState
import app.vercors.feature.instance.domain.model.Instance
import app.vercors.feature.instance.domain.model.options.DefaultOptions
import app.vercors.feature.instance.domain.model.options.OptionsCategory
import app.vercors.feature.instance.domain.model.options.OptionsSource
import app.vercors.feature.instance.domain.usecase.CopyOptionsUseCase
import app.vercors.feature.instance.domain.usecase.ObserveDefaultOptionsUseCase
import app.vercors.feature.instance.domain.usecase.ObserveInstanceListUseCase
import app.vercors.feature.instance.ui.InstanceRes
import app.vercors.feature.instance.ui.message
import app.vercors.feature.instance.ui.options.DEFAULT_SELECTED_OPTIONS_CATEGORIES
import app.vercors.feature.instance.ui.optionsCopied
import app.vercors.feature.instance.ui.optionsCopiedKeybindsSkipped
import app.vercors.shared.ui.*
import arrow.core.raise.context.either
import com.slack.circuit.overlay.OverlayNavigator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import opensavvy.state.progressive.valueOrNull
import org.jetbrains.compose.resources.stringResource

class CopyOptionsPresenter(
    private val target: CopyOptionsTarget,
    private val navigator: OverlayNavigator<CopyOptionsResult>,
    private val globalScope: CoroutineScope,
    private val observeInstanceList: ObserveInstanceListUseCase,
    private val observeDefaultOptions: ObserveDefaultOptionsUseCase,
    private val copyOptions: CopyOptionsUseCase,
    private val snackbarService: SnackbarService,
) : Presenter<CopyOptionsState, CopyOptionsEvent> {
    @Composable
    override fun present(): Store<CopyOptionsState, CopyOptionsEvent> {
        val instances by observeInstanceList().collectProgressiveAsState()
        val defaultOptions by remember { observeDefaultOptions() }.collectAsState(null)
        val instanceList = instances.valueOrNull?.mapNotNull { it.getOrNull()?.instance }
        val sources = remember(instanceList, defaultOptions) { instanceList?.toSourceViews(defaultOptions) }
        val (selectedSource, setSelectedSource) = remember { mutableStateOf<CopyOptionsSourceView?>(null) }
        val (categories, setCategories) = remember { mutableStateOf(DEFAULT_SELECTED_OPTIONS_CATEGORIES) }
        val (isCopying, setIsCopying) = remember { mutableStateOf(false) }
        val effectiveSource = selectedSource?.takeIf { sources?.contains(it) == true } ?: sources?.firstOrNull()

        return CopyOptionsState(
            targetGameVersion = target.gameVersion,
            sources = sources,
            selectedSource = effectiveSource,
            categories = categories,
            isCopying = isCopying,
        ) + { event ->
            handleEvent(
                event = event,
                selectedSource = effectiveSource,
                setSelectedSource = setSelectedSource,
                categories = categories,
                setCategories = setCategories,
                setIsCopying = setIsCopying,
            )
        }
    }

    private fun handleEvent(
        event: CopyOptionsEvent,
        selectedSource: CopyOptionsSourceView?,
        setSelectedSource: (CopyOptionsSourceView?) -> Unit,
        categories: Set<OptionsCategory>,
        setCategories: (Set<OptionsCategory>) -> Unit,
        setIsCopying: (Boolean) -> Unit,
    ) {
        when (event) {
            is CopyOptionsEvent.OnSourceSelected -> setSelectedSource(event.source)
            is CopyOptionsEvent.OnCategoryToggle ->
                setCategories(if (event.checked) categories + event.category else categories - event.category)

            CopyOptionsEvent.OnCopyClick -> selectedSource?.let { handleCopy(it.source, categories, setIsCopying) }
            CopyOptionsEvent.OnCancelClick -> navigator.finish(CopyOptionsResult.Cancelled)
        }
    }

    private fun handleCopy(source: OptionsSource, categories: Set<OptionsCategory>, setIsCopying: (Boolean) -> Unit) {
        setIsCopying(true)
        globalScope.launch {
            either { copyOptions(source, target.id, categories) }
                .onLeft { error ->
                    setIsCopying(false)
                    snackbarService.error { error.message }
                }
                .onRight { result ->
                    if (OptionsCategory.Keybinds in result.skippedCategories) {
                        snackbarService.warn { stringResource(InstanceRes.string.optionsCopiedKeybindsSkipped) }
                    } else {
                        snackbarService.success { stringResource(InstanceRes.string.optionsCopied) }
                    }
                    navigator.finish(CopyOptionsResult.Copied)
                }
        }
    }

    private fun List<Instance>.toSourceViews(defaultOptions: DefaultOptions?): List<CopyOptionsSourceView> {
        val defaultSource = defaultOptions?.let {
            CopyOptionsSourceView(source = OptionsSource.Default, name = null, gameVersion = it.gameVersion)
        }
        val instanceSources = filter { it.id != target.id }
            .sortedByDescending { it.lastPlayedAt ?: it.createdAt }
            .map { CopyOptionsSourceView(OptionsSource.Instance(it.id), it.name, it.gameVersion) }
        return listOfNotNull(defaultSource) + instanceSources
    }
}
