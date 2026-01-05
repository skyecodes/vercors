package app.vercors.feature.account.data.repository

import app.vercors.core.domain.*
import app.vercors.feature.account.data.datasource.AccountDataSource
import app.vercors.feature.account.data.dto.toData
import app.vercors.feature.account.data.dto.toDomain
import app.vercors.feature.account.domain.model.Account
import app.vercors.feature.account.domain.model.AccountId
import app.vercors.feature.account.domain.model.AccountList
import app.vercors.feature.account.domain.repository.AccountNotFoundException
import app.vercors.feature.account.domain.repository.AccountRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlin.time.Clock

private val logger = KotlinLogging.logger {}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class AccountRepositoryImpl(
    coroutineScope: CoroutineScope,
    private val dataSource: AccountDataSource,
) : AccountRepository {
    private val _state: MutableResourceStateFlow<AccountList> = MutableResourceStateFlow()
    override val state: ResourceStateFlow<AccountList> = _state.asResourceStateFlow(
        coroutineScope = coroutineScope,
        onStart = {
            coroutineScope.launch {
                dataSource.accountState.collect { data ->
                    _state.update { data.map { it.toDomain() } }
                }
            }
        },
        onEach = { logger.trace { "Account list: $it" } }
    )
    private val mutex = Mutex()

    override suspend fun addAccount(account: Account) {
        _state.read(mutex) {
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
        _state.read(mutex) {
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
        _state.read(mutex) {
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
        state.await().orThrow().accounts.find { it.id == id } ?: throw AccountNotFoundException(id)

    override suspend fun getSelectedAccount(): Account? =
        state.await().orThrow().let { it.accounts.firstOrNull { acc -> acc.id == it.selected } }
}
