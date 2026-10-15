package app.vercors.feature.instance.ui.options.savedefault

import androidx.compose.runtime.*
import app.vercors.feature.instance.domain.model.options.OptionsCategory
import app.vercors.feature.instance.domain.usecase.ObserveDefaultOptionsUseCase
import app.vercors.feature.instance.domain.usecase.SaveDefaultOptionsUseCase
import app.vercors.feature.instance.ui.InstanceRes
import app.vercors.feature.instance.ui.defaultOptionsSaved
import app.vercors.feature.instance.ui.message
import app.vercors.feature.instance.ui.options.DEFAULT_SELECTED_OPTIONS_CATEGORIES
import app.vercors.shared.ui.*
import arrow.core.raise.context.either
import com.slack.circuit.overlay.OverlayNavigator
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@AssistedInject
class SaveDefaultOptionsPresenter(
    @Assisted private val target: SaveDefaultOptionsTarget,
    @Assisted private val navigator: OverlayNavigator<SaveDefaultOptionsResult>,
    private val globalScope: CoroutineScope,
    private val observeDefaultOptions: ObserveDefaultOptionsUseCase,
    private val saveDefaultOptions: SaveDefaultOptionsUseCase,
    private val snackbarService: SnackbarService,
) : Presenter<SaveDefaultOptionsState, SaveDefaultOptionsEvent> {
    @Composable
    override fun present(): Store<SaveDefaultOptionsState, SaveDefaultOptionsEvent> {
        val defaultOptions by remember { observeDefaultOptions() }.collectAsState(null)
        val (categories, setCategories) = remember { mutableStateOf(DEFAULT_SELECTED_OPTIONS_CATEGORIES) }
        val (isSaving, setIsSaving) = remember { mutableStateOf(false) }

        return SaveDefaultOptionsState(
            categories = categories,
            replacedSourceName = defaultOptions?.sourceInstanceName,
            isSaving = isSaving,
        ) + { event ->
            handleEvent(
                event = event,
                categories = categories,
                setCategories = setCategories,
                setIsSaving = setIsSaving,
            )
        }
    }

    private fun handleEvent(
        event: SaveDefaultOptionsEvent,
        categories: Set<OptionsCategory>,
        setCategories: (Set<OptionsCategory>) -> Unit,
        setIsSaving: (Boolean) -> Unit,
    ) {
        when (event) {
            is SaveDefaultOptionsEvent.OnCategoryToggle ->
                setCategories(if (event.checked) categories + event.category else categories - event.category)

            SaveDefaultOptionsEvent.OnSaveClick -> handleSave(categories, setIsSaving)
            SaveDefaultOptionsEvent.OnCancelClick -> navigator.finish(SaveDefaultOptionsResult.Cancelled)
        }
    }

    private fun handleSave(categories: Set<OptionsCategory>, setIsSaving: (Boolean) -> Unit) {
        setIsSaving(true)
        globalScope.launch {
            either { saveDefaultOptions(target.id, categories) }
                .onLeft { error ->
                    setIsSaving(false)
                    snackbarService.error { error.message }
                }
                .onRight {
                    snackbarService.success { stringResource(InstanceRes.string.defaultOptionsSaved) }
                    navigator.finish(SaveDefaultOptionsResult.Saved)
                }
        }
    }

    @AssistedFactory
    fun interface Factory {
        fun create(
            target: SaveDefaultOptionsTarget,
            navigator: OverlayNavigator<SaveDefaultOptionsResult>
        ): SaveDefaultOptionsPresenter
    }
}
