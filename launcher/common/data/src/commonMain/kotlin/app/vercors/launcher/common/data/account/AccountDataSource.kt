package app.vercors.launcher.common.data.account

import app.vercors.launcher.common.data.AppPath
import app.vercors.launcher.common.data.IODispatcher
import app.vercors.launcher.common.data.deleteRecursively
import io.github.irgaly.kfswatch.KfsDirectoryWatcher
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.io.decodeFromSource
import kotlinx.serialization.json.io.encodeToSink
import org.koin.core.annotation.Single

private val logger = KotlinLogging.logger {}

internal const val ACCOUNT_LIST_FILE_NAME = "accounts.json"

@Single
internal class AccountDataSource(
    coroutineScope: CoroutineScope,
    @AppPath private val appPath: Path,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    private val json: Json,
    private val watcher: KfsDirectoryWatcher
) {
    internal val accountsFile = Path(appPath, ACCOUNT_LIST_FILE_NAME)
    val updates: Flow<AccountListData> =
        watcher.onEventFlow.filter { it.path == ACCOUNT_LIST_FILE_NAME }.mapNotNull { loadFromFile() }

    init {
        coroutineScope.launch {
            watcher.add(appPath.toString())
        }
    }

    suspend fun loadFromFile(): AccountListData? = withContext(ioDispatcher) {
        runCatching {
            if (SystemFileSystem.exists(accountsFile)) {
                if (SystemFileSystem.metadataOrNull(accountsFile)?.isRegularFile == true) {
                    logger.debug { "Loading account list file $accountsFile" }
                    return@withContext SystemFileSystem.source(accountsFile).buffered().use {
                        json.decodeFromSource<AccountListData>(it)
                    }
                } else {
                    logger.warn { "Illegal file found at location $accountsFile" }
                    SystemFileSystem.deleteRecursively(accountsFile)
                }
            }
        }.onFailure { logger.error(it) { "Error loading account list file $accountsFile" } }
        return@withContext null
    }

    suspend fun saveToFile(accountListData: AccountListData) = withContext(ioDispatcher) {
        if (SystemFileSystem.exists(accountsFile) && SystemFileSystem.metadataOrNull(accountsFile)?.isRegularFile != true) {
            logger.warn { "Illegal file found at location $accountsFile" }
            SystemFileSystem.deleteRecursively(accountsFile)
        }
        logger.debug { "Saving account list file $accountsFile" }
        SystemFileSystem.createDirectories(accountsFile.parent!!)
        SystemFileSystem.sink(accountsFile).buffered().use {
            json.encodeToSink(accountListData, it)
        }
    }
}
