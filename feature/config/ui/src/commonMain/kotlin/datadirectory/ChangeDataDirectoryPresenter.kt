package app.vercors.feature.config.ui.datadirectory

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import app.vercors.core.domain.AppConstants
import app.vercors.core.domain.toFractionOrNull
import app.vercors.core.filesystem.withAppFolderName
import app.vercors.feature.config.domain.model.DataDirectoryError
import app.vercors.feature.config.domain.usecase.MoveDataDirectoryUseCase
import app.vercors.feature.config.ui.ConfigRes
import app.vercors.feature.config.ui.changeDataDirectoryCopyFailed
import app.vercors.feature.config.ui.changeDataDirectoryUnreachable
import app.vercors.shared.ui.AppRestartService
import app.vercors.shared.ui.Presenter
import app.vercors.shared.ui.Store
import arrow.core.raise.context.either
import com.slack.circuit.overlay.OverlayNavigator
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.Named
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.io.files.Path
import org.jetbrains.compose.resources.stringResource

@AssistedInject
class ChangeDataDirectoryPresenter(
    @Assisted private val currentPath: String,
    @Named(AppConstants.APP_ID_KEY) private val appId: String,
    @Assisted private val navigator: OverlayNavigator<Unit>,
    private val globalScope: CoroutineScope,
    private val moveDataDirectory: MoveDataDirectoryUseCase,
    private val appRestartService: AppRestartService,
) : Presenter<ChangeDataDirectoryState, ChangeDataDirectoryEvent> {
    @Composable
    override fun present(): Store<ChangeDataDirectoryState, ChangeDataDirectoryEvent> {
        val (pathText, setPathText) = remember { mutableStateOf(currentPath) }
        val (isMoving, setIsMoving) = remember { mutableStateOf(false) }
        val (moveProgress, setMoveProgress) = remember { mutableStateOf<Float?>(null) }
        val (errorMessage, setErrorMessage) = remember { mutableStateOf<String?>(null) }
        val unreachableMessage = stringResource(ConfigRes.string.changeDataDirectoryUnreachable)
        val copyFailedMessage = stringResource(ConfigRes.string.changeDataDirectoryCopyFailed)

        return ChangeDataDirectoryState(
            pathText = pathText,
            isMoving = isMoving,
            moveProgress = moveProgress,
            errorMessage = errorMessage,
        ) + { event ->
            handleEvent(
                event = event,
                pathText = pathText,
                setPathText = setPathText,
                setIsMoving = setIsMoving,
                setMoveProgress = setMoveProgress,
                setErrorMessage = setErrorMessage,
                unreachableMessage = unreachableMessage,
                copyFailedMessage = copyFailedMessage,
            )
        }
    }

    private fun handleEvent(
        event: ChangeDataDirectoryEvent,
        pathText: String,
        setPathText: (String) -> Unit,
        setIsMoving: (Boolean) -> Unit,
        setMoveProgress: (Float?) -> Unit,
        setErrorMessage: (String?) -> Unit,
        unreachableMessage: String,
        copyFailedMessage: String,
    ) {
        when (event) {
            is ChangeDataDirectoryEvent.OnPathChange -> {
                setPathText(event.value)
                setErrorMessage(null)
            }

            ChangeDataDirectoryEvent.OnResetClick -> {
                setPathText(currentPath)
                setErrorMessage(null)
            }

            ChangeDataDirectoryEvent.OnCancelClick -> navigator.finish(Unit)

            ChangeDataDirectoryEvent.OnConfirmClick -> handleConfirm(
                pathText = pathText,
                setIsMoving = setIsMoving,
                setMoveProgress = setMoveProgress,
                setErrorMessage = setErrorMessage,
                unreachableMessage = unreachableMessage,
                copyFailedMessage = copyFailedMessage,
            )
        }
    }

    private fun handleConfirm(
        pathText: String,
        setIsMoving: (Boolean) -> Unit,
        setMoveProgress: (Float?) -> Unit,
        setErrorMessage: (String?) -> Unit,
        unreachableMessage: String,
        copyFailedMessage: String,
    ) {
        val typed = pathText.trim().trimEnd('/', '\\').ifEmpty { currentPath }
        val chosen = withAppFolderName(appId, Path(typed))
        if (chosen.toString() == withAppFolderName(appId, Path(currentPath)).toString()) {
            navigator.finish(Unit)
            return
        }

        setIsMoving(true)
        setMoveProgress(0f)
        setErrorMessage(null)
        globalScope.launch {
            either { moveDataDirectory(chosen.toString()) { progress -> setMoveProgress(progress.toFractionOrNull()) } }
                .onLeft { error ->
                    setIsMoving(false)
                    setMoveProgress(null)
                    setErrorMessage(
                        when (error) {
                            DataDirectoryError.Unreachable -> unreachableMessage
                            DataDirectoryError.CopyFailed -> copyFailedMessage
                        }
                    )
                }
                .onRight { appRestartService.restart() }
        }
    }

    @AssistedFactory
    fun interface Factory {
        fun create(currentPath: String, navigator: OverlayNavigator<Unit>): ChangeDataDirectoryPresenter
    }
}

