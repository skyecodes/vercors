package app.vercors.feature.config.ui.gameoptions

import androidx.compose.runtime.*
import app.vercors.feature.instance.domain.usecase.ClearDefaultOptionsUseCase
import app.vercors.feature.instance.domain.usecase.ObserveDefaultOptionsUseCase
import app.vercors.feature.instance.domain.usecase.SetDefaultOptionsAutoApplyUseCase
import app.vercors.feature.instance.ui.message
import app.vercors.shared.ui.Presenter
import app.vercors.shared.ui.SnackbarService
import app.vercors.shared.ui.Store
import app.vercors.shared.ui.error
import arrow.core.raise.context.either
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Inject
class GameOptionsConfigurationPresenter(
    private val observeDefaultOptions: ObserveDefaultOptionsUseCase,
    private val setDefaultOptionsAutoApply: SetDefaultOptionsAutoApplyUseCase,
    private val clearDefaultOptions: ClearDefaultOptionsUseCase,
    private val snackbarService: SnackbarService,
) : Presenter<GameOptionsConfigurationState, GameOptionsConfigurationEvent> {
    @Composable
    override fun present(): Store<GameOptionsConfigurationState, GameOptionsConfigurationEvent> {
        val presenterScope = rememberCoroutineScope()
        val defaultOptions by remember { observeDefaultOptions() }.collectAsState(null)
        val (promptClearDefaultOptions, setPromptClearDefaultOptions) = remember { mutableStateOf(false) }
        val defaultOptionsView = defaultOptions?.toView()

        return GameOptionsConfigurationState(
            defaultOptions = defaultOptionsView,
            promptClearDefaultOptions = promptClearDefaultOptions,
        ) + { event ->
            handleEvent(
                event = event,
                presenterScope = presenterScope,
                autoApply = defaultOptionsView?.autoApply,
                setPromptClearDefaultOptions = setPromptClearDefaultOptions,
            )
        }
    }

    private fun handleEvent(
        event: GameOptionsConfigurationEvent,
        presenterScope: CoroutineScope,
        autoApply: Boolean?,
        setPromptClearDefaultOptions: (Boolean) -> Unit,
    ) {
        when (event) {
            GameOptionsConfigurationEvent.OnAutoApplyToggle -> autoApply?.let {
                handleAutoApplyToggle(
                    !it,
                    presenterScope
                )
            }

            GameOptionsConfigurationEvent.OnClearClick -> setPromptClearDefaultOptions(true)
            is GameOptionsConfigurationEvent.OnConfirmClear ->
                handleConfirmClear(event.result, presenterScope, setPromptClearDefaultOptions)
        }
    }

    private fun handleAutoApplyToggle(autoApply: Boolean, presenterScope: CoroutineScope) {
        presenterScope.launch {
            either { setDefaultOptionsAutoApply(autoApply) }
                .onLeft { error -> snackbarService.error { error.message } }
        }
    }

    private fun handleConfirmClear(
        confirmed: Boolean,
        presenterScope: CoroutineScope,
        setPromptClearDefaultOptions: (Boolean) -> Unit,
    ) {
        setPromptClearDefaultOptions(false)
        if (!confirmed) return
        presenterScope.launch {
            either { clearDefaultOptions() }
                .onLeft { error -> snackbarService.error { error.message } }
        }
    }
}
