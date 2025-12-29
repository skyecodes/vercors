package app.vercors.launcher.common.data.account

import app.vercors.launcher.common.domain.account.AccountNotFoundException
import app.vercors.launcher.common.domain.account.AccountRepository
import app.vercors.launcher.common.domain.account.model.Account
import app.vercors.launcher.common.domain.account.model.AccountId
import app.vercors.launcher.common.domain.account.model.AccountList
import app.vercors.shared.domain.*
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import org.koin.core.annotation.Single
import kotlin.time.Clock

private val logger = KotlinLogging.logger {}

@Single
internal class AccountRepositoryImpl(
    coroutineScope: CoroutineScope,
    private val dataSource: AccountDataSource,
) : AccountRepository {
    private val _state: MutableResourceStateFlow<AccountList> = MutableResourceStateFlow()
    override val state: ResourceStateFlow<AccountList> = _state.asResourceStateFlow(
        coroutineScope = coroutineScope,
        onStart = {
            coroutineScope.launch {
                dataSource.updates.collect { data ->
                    _state.update { Resource.Success(data.toDomain()) }
                }
            }
        },
        onEach = { logger.trace { "Account list: $it" } }
    )
    private val mutex = Mutex()

    init {
        coroutineScope.launch { load() }
    }

    override suspend fun load() {
        _state.load(mutex) {
            dataSource.loadFromFile()?.toDomain() ?: AccountList()
        }
    }

    override suspend fun addAccount(account: Account) {
        _state.update(mutex) {
            val newAccounts = it.accounts.filter { acc -> acc.id != account.id } + account
            dataSource.saveToFile(
                it.copy(
                    selected = account.id,
                    accounts = newAccounts
                ).toData()
            )
        }
    }

    override suspend fun removeAccount(account: Account) {
        _state.update(mutex) {
            val newAccounts = it.accounts.filter { acc -> acc.id != account.id }
            dataSource.saveToFile(
                it.copy(
                    selected = newAccounts.firstOrNull()?.id,
                    accounts = newAccounts
                ).toData()
            )
        }
    }

    override suspend fun selectAccount(account: Account) {
        _state.update(mutex) {
            dataSource.saveToFile(
                it.copy(
                    selected = account.id,
                    accounts = it.accounts
                        .map { acc -> if (acc.id == account.id) acc.copy(dateSelected = Clock.System.now()) else acc }
                        .sortedByDescending { acc -> acc.dateSelected }
                ).toData())
        }
    }

    override suspend fun findAccount(id: AccountId): Account =
        state.awaitValueOrThrow().accounts.find { it.id == id } ?: throw AccountNotFoundException(id)

    override suspend fun getSelectedAccount(): Account? =
        state.awaitValueOrThrow().let { it.accounts.firstOrNull { acc -> acc.id == it.selected } }
}
