package app.vercors.feature.account.domain.model

data class AccountList(
    val selected: AccountId? = null,
    val accounts: List<Account> = emptyList(),
) {
    val selectedAccount: Account? = accounts.firstOrNull { it.id == selected }
}
