package app.vercors.shared.data

import app.vercors.core.data.deleteRecursively
import app.vercors.core.data.isRegularFile
import app.vercors.core.domain.Resource
import app.vercors.core.domain.resultOrNull
import io.github.irgaly.kfswatch.KfsDirectoryWatcher
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.files.Path

private val logger = KotlinLogging.logger {}

class LocalOnlyFileStore<Domain, Local>(
    filePath: Path,
    private val localToDomain: Local.() -> Domain,
    private val domainToLocal: Domain.() -> Local,
    private val loadLocal: suspend (Path) -> Local,
    private val saveLocal: suspend (Path, Local) -> Unit,
    private val defaultProvider: () -> Domain,
    coroutineScope: CoroutineScope,
    kfsWatcher: KfsDirectoryWatcher
) : AbstractFileStore<Domain>(filePath, coroutineScope, kfsWatcher) {
    private val mutex = Mutex()

    override suspend fun reloadFile() {
        if (mutex.isLocked) return
        mutex.withLock {
            mutableState.update { Resource.Loading(it.resultOrNull()) }
            val result = try {
                if (!filePath.isRegularFile()) {
                    logger.debug { "No file found at $filePath - writing default data" }
                    filePath.deleteRecursively()
                    saveLocal(filePath, defaultProvider().domainToLocal())
                    return
                }
                logger.debug { "Reading data at $filePath" }
                Resource.Success(loadLocal(filePath).localToDomain())
            } catch (e: Exception) {
                currentCoroutineContext().ensureActive()
                delay(1000) // If another update happens within 1 second, the failure will be canceled
                Resource.Failure(e)
            }
            mutableState.emit(result)
        }
    }
}
