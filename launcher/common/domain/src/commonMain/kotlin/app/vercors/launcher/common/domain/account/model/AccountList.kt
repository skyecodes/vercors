package app.vercors.launcher.common.domain.account.model

import kotlin.uuid.Uuid

data class AccountList(
    val selected: Uuid? = null,
    val accounts: List<Account> = emptyList(),
) {
    val selectedAccount: Account? = accounts.firstOrNull { it.uuid == selected }
}
