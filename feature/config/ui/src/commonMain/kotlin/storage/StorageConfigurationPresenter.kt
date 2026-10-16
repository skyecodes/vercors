package app.vercors.feature.config.ui.storage

import androidx.compose.runtime.*
import app.vercors.core.system.SystemService
import app.vercors.feature.config.domain.usecase.ClearContentCacheUseCase
import app.vercors.feature.config.domain.usecase.GetContentCacheSizeUseCase
import app.vercors.feature.config.domain.usecase.GetDataDirectoryUseCase
import app.vercors.feature.config.ui.ConfigRes
import app.vercors.feature.config.ui.collectHasRunningInstance
import app.vercors.feature.config.ui.contentCacheCleared
import app.vercors.feature.config.ui.datadirectory.ChangeDataDirectoryDialog
import app.vercors.feature.config.ui.errorOpenDataDirectoryFailed
import app.vercors.feature.instance.domain.usecase.ObserveInstanceListUseCase
import app.vercors.shared.ui.*
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Inject
class StorageConfigurationPresenter(
    private val systemService: SystemService,
    private val snackbarService: SnackbarService,
    private val getContentCacheSize: GetContentCacheSizeUseCase,
    private val clearContentCache: ClearContentCacheUseCase,
    private val getDataDirectory: GetDataDirectoryUseCase,
    private val observeInstanceList: ObserveInstanceListUseCase,
    private val changeDataDirectoryDialogFactory: ChangeDataDirectoryDialog.Factory,
) : Presenter<StorageConfigurationState, StorageConfigurationEvent> {
    @Composable
    override fun present(): Store<StorageConfigurationState, StorageConfigurationEvent> {
        val presenterScope = rememberCoroutineScope()
        val (contentCacheSizeBytes, setContentCacheSizeBytes) = remember { mutableStateOf<Long?>(null) }
        LaunchedEffect(Unit) { setContentCacheSizeBytes(getContentCacheSize()) }
        val (promptClearContentCache, setPromptClearContentCache) = remember { mutableStateOf(false) }
        val dataDirectory = remember { getDataDirectory() }
        val hasRunningInstance = observeInstanceList.collectHasRunningInstance()
        val (promptChangeDataDirectory, setPromptChangeDataDirectory) = remember { mutableStateOf(false) }
        val changeDataDirectoryDialog =
            remember(dataDirectory) { changeDataDirectoryDialogFactory.create(dataDirectory) }

        return StorageConfigurationState(
            dataDirectory = dataDirectory,
            canChangeDataDirectory = !hasRunningInstance,
            promptChangeDataDirectory = promptChangeDataDirectory,
            changeDataDirectoryDialog = changeDataDirectoryDialog,
            contentCacheSizeBytes = contentCacheSizeBytes,
            promptClearContentCache = promptClearContentCache,
        ) + { event ->
            handleEvent(
                event = event,
                presenterScope = presenterScope,
                setPromptClearContentCache = setPromptClearContentCache,
                setContentCacheSizeBytes = setContentCacheSizeBytes,
                dataDirectory = dataDirectory,
                setPromptChangeDataDirectory = setPromptChangeDataDirectory,
            )
        }
    }

    private fun handleEvent(
        event: StorageConfigurationEvent,
        presenterScope: CoroutineScope,
        setPromptClearContentCache: (Boolean) -> Unit,
        setContentCacheSizeBytes: (Long?) -> Unit,
        dataDirectory: String,
        setPromptChangeDataDirectory: (Boolean) -> Unit,
    ) {
        when (event) {
            StorageConfigurationEvent.OnClearContentCacheClick -> setPromptClearContentCache(true)

            is StorageConfigurationEvent.OnConfirmClearContentCache -> handleConfirmClearContentCache(
                event = event,
                presenterScope = presenterScope,
                setPromptClearContentCache = setPromptClearContentCache,
                setContentCacheSizeBytes = setContentCacheSizeBytes,
            )

            StorageConfigurationEvent.OnOpenDataDirectoryClick -> handleOpenDataDirectory(dataDirectory, presenterScope)

            StorageConfigurationEvent.OnChangeDataDirectoryClick -> setPromptChangeDataDirectory(true)
            StorageConfigurationEvent.OnChangeDataDirectoryDismiss -> setPromptChangeDataDirectory(false)
        }
    }

    private fun handleOpenDataDirectory(dataDirectory: String, presenterScope: CoroutineScope) {
        presenterScope.launch {
            if (!systemService.openInFileManager(dataDirectory)) {
                snackbarService.error { stringResource(ConfigRes.string.errorOpenDataDirectoryFailed) }
            }
        }
    }

    private fun handleConfirmClearContentCache(
        event: StorageConfigurationEvent.OnConfirmClearContentCache,
        presenterScope: CoroutineScope,
        setPromptClearContentCache: (Boolean) -> Unit,
        setContentCacheSizeBytes: (Long?) -> Unit,
    ) {
        setPromptClearContentCache(false)
        if (!event.result) return
        presenterScope.launch {
            clearContentCache()
            setContentCacheSizeBytes(getContentCacheSize())
            snackbarService.success { stringResource(ConfigRes.string.contentCacheCleared) }
        }
    }
}
