package app.vercors.shared.data

import app.vercors.core.domain.MutableResourceStateFlow
import app.vercors.core.domain.asResourceStateFlow
import io.github.irgaly.kfswatch.KfsDirectoryWatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.io.files.Path

abstract class AbstractFileStore<Domain>(
    protected val filePath: Path,
    protected val coroutineScope: CoroutineScope,
    protected val kfsWatcher: KfsDirectoryWatcher
) {
    protected val mutableState = MutableResourceStateFlow<Domain>()
    val state = mutableState.asResourceStateFlow(
        coroutineScope = coroutineScope,
        onStart = { onStart() },
        onCompletion = { onStop() }
    )

    protected suspend fun onStart() {
        kfsWatcher.add(filePath.toString())
        coroutineScope.launch {
            kfsWatcher.onEventFlow
                .filter { event -> event.path == filePath.name }
                .onStart { reloadFile() }
                .collectLatest { reloadFile() }
        }
    }

    protected suspend fun onStop() {
        kfsWatcher.remove(filePath.toString())
    }

    protected abstract suspend fun reloadFile()
}
