package app.vercors.feature.account.domain.repository

import app.vercors.core.domain.ResourceStateFlow
import app.vercors.feature.account.domain.model.Account
import app.vercors.feature.account.domain.model.AccountId
import app.vercors.feature.account.domain.model.AccountList

interface AccountRepository {
    val state: ResourceStateFlow<AccountList>

    suspend fun load()
    suspend fun addAccount(account: Account)
    suspend fun removeAccount(account: Account)
    suspend fun selectAccount(account: Account)
    suspend fun findAccount(id: AccountId): Account
    suspend fun getSelectedAccount(): Account?
}

class AccountNotFoundException(accountId: AccountId) :
    IllegalArgumentException("Account with ID '$accountId' not found")
