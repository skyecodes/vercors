package app.vercors.launcher.common.domain.account

import app.vercors.launcher.common.domain.account.model.Account
import app.vercors.launcher.common.domain.account.model.AccountList
import app.vercors.shared.domain.ResourceStateFlow

interface AccountRepository {
    val state: ResourceStateFlow<AccountList>

    suspend fun load()
    suspend fun addAccount(account: Account)
    suspend fun removeAccount(account: Account)
    suspend fun selectAccount(account: Account)
}
