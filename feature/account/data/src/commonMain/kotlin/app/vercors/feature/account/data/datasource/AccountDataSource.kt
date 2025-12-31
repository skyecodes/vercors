package app.vercors.feature.account.data.datasource

import app.vercors.core.data.deleteRecursively
import app.vercors.core.data.readJson
import app.vercors.core.data.writeJson
import app.vercors.feature.account.data.dto.AccountListData
import app.vercors.shared.data.IODispatcher
import io.github.irgaly.kfswatch.KfsDirectoryWatcher
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.mapLatest
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single

private val logger = KotlinLogging.logger {}

internal const val ACCOUNT_LIST_FILE_NAME = "accounts.json"

@Single
internal class AccountDataSource(
    coroutineScope: CoroutineScope,
    private val appPath: Path,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    private val json: Json,
    private val watcher: KfsDirectoryWatcher
) {
    internal val accountsFile = Path(appPath, ACCOUNT_LIST_FILE_NAME)

    @OptIn(ExperimentalCoroutinesApi::class)
    val updates: Flow<AccountListData> =
        watcher.onEventFlow.filter { it.path == ACCOUNT_LIST_FILE_NAME }.mapLatest { loadFromFile() }.filterNotNull()

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
                    return@withContext accountsFile.readJson<AccountListData>(json, ioDispatcher)
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
        accountsFile.writeJson(accountListData, json, ioDispatcher)
    }
}
