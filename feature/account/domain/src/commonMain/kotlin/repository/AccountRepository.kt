package app.vercors.feature.account.domain.repository

import app.vercors.core.domain.ProgressiveStateFlow
import app.vercors.feature.account.domain.model.Account
import app.vercors.feature.account.domain.model.AccountError
import app.vercors.feature.account.domain.model.AccountId
import app.vercors.feature.account.domain.model.AccountLoadingError
import arrow.core.raise.Raise

interface AccountRepository {
    val state: ProgressiveStateFlow<AccountLoadingError, List<Account>>

    suspend fun addAccount(account: Account)

    suspend fun removeAccount(account: Account)

    suspend fun selectAccount(account: Account)

    context(_: Raise<AccountError>)
    suspend fun findAccount(id: AccountId): Account

    context(_: Raise<AccountLoadingError>)
    suspend fun getSelectedAccount(): Account?
}
