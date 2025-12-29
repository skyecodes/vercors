package app.vercors.launcher.common.domain.account.model

data class AccountList(
    val selected: AccountId? = null,
    val accounts: List<Account> = emptyList(),
) {
    val selectedAccount: Account? = accounts.firstOrNull { it.id == selected }
}
