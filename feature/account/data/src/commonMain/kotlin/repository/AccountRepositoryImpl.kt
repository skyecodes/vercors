package app.vercors.feature.account.data.repository

import app.vercors.core.cryptography.CryptographicKey
import app.vercors.core.cryptography.CryptographyService
import app.vercors.core.domain.ProgressiveStateFlow
import app.vercors.core.domain.progressiveStateIn
import app.vercors.core.filesystem.*
import app.vercors.feature.account.data.datasource.AccountDataSource
import app.vercors.feature.account.data.dto.AccountData
import app.vercors.feature.account.data.dto.toData
import app.vercors.feature.account.data.dto.toDomain
import app.vercors.feature.account.domain.model.*
import app.vercors.feature.account.domain.repository.AccountRepository
import arrow.core.raise.Raise
import arrow.core.raise.context.bind
import arrow.core.raise.context.ensureNotNull
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.bytestring.hexToByteString
import kotlinx.io.bytestring.toHexString
import kotlinx.io.files.Path
import opensavvy.state.arrow.toEither
import opensavvy.state.coroutines.now
import opensavvy.state.outcome.onSuccess
import opensavvy.state.progressive.ProgressiveOutcome
import opensavvy.state.progressive.map
import opensavvy.state.progressive.mapFailure
import kotlin.time.Clock

private val logger = KotlinLogging.logger {}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class AccountRepositoryImpl(
    coroutineScope: CoroutineScope,
    appPath: Path,
    private val dataSource: AccountDataSource,
    private val cryptographyService: CryptographyService
) : AccountRepository {
    private val mutex = Mutex()
    override val state: ProgressiveStateFlow<AccountLoadingError, List<Account>> = dataSource.flow
        .map {
            it.toDomain()
        }.onEach {
            logger.trace { "Account list: $it" }
        }.progressiveStateIn(coroutineScope)
    private val keyFile = Path(appPath, "key.priv")

    override suspend fun addAccount(account: Account) {
        mutex.withLock {
            state.now().onSuccess { list ->
                dataSource.write(
                    (list
                        .filter { acc -> acc.id != account.id } + account)
                        .sortToData()
                )
            }
        }
    }

    override suspend fun removeAccount(account: Account) {
        mutex.withLock {
            state.now().onSuccess { list ->
                dataSource.write(
                    list
                        .filter { acc -> acc.id != account.id }
                        .sortToData()
                )
            }
        }
    }

    override suspend fun selectAccount(account: Account) {
        mutex.withLock {
            state.now().onSuccess { list ->
                dataSource.write(
                    list
                        .map { acc -> if (acc.id == account.id) acc.copy(dateSelected = Clock.System.now()) else acc }
                        .sortToData()
                )
            }
        }
    }

    private suspend fun getEncryptionKey(): CryptographicKey {
        if (!keyFile.isRegularFile()) {
            if (keyFile.exists()) keyFile.deleteRecursively()
            val key = cryptographyService.generateKey()
            keyFile.writeData(cryptographyService.encodeKey(key))
        }
        return cryptographyService.decodeKey(keyFile.readData())
    }

    private suspend fun List<Account>.sortToData(): List<AccountData> =
        sortedByDescending { acc -> acc.dateSelected }.map { acc ->
            acc.toData {
                cryptographyService.encode(getEncryptionKey(), it).toHexString()
            }
        }

    context(_: Raise<AccountError>)
    override suspend fun findAccount(id: AccountId): Account =
        ensureNotNull(state.now().toEither().bind().find { it.id == id }) { AccountNotFound(id) }

    context(_: Raise<AccountLoadingError>)
    override suspend fun getSelectedAccount(): Account? = state.now().toEither().bind().firstOrNull()

    private suspend fun ProgressiveOutcome<FileStoreError, List<AccountData>>.toDomain(): ProgressiveOutcome<AccountLoadingError, List<Account>> =
        map { list ->
            list.map { acc ->
                acc.toDomain {
                    cryptographyService.decode(getEncryptionKey(), it.hexToByteString())
                }
            }
        }.mapFailure { AccountLoadingError(it) }
}
