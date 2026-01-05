package app.vercors.feature.account.ui.list

import androidx.compose.runtime.Immutable
import app.vercors.core.domain.Resource
import app.vercors.feature.account.domain.model.AccountId
import app.vercors.feature.account.domain.model.AccountSkin

@Immutable
data class AccountListState(
    val accountList: Resource<AccountListView> = Resource.Unloaded,
    val loginState: LoginStateView = LoginStateView(),
)

@Immutable
data class AccountListView(
    val selected: AccountId?,
    val accounts: List<AccountView>,
) {
    val selectedAccount: AccountView? = accounts.firstOrNull { it.id == selected }
    val otherAccounts: List<AccountView> = accounts.filter { it.id != selected }
}

@Immutable
data class AccountView(
    val id: AccountId,
    val username: String,
    val skin: AccountSkin
)

@Immutable
data class LoginStateView(
    val isLoggingIn: Boolean = false,
    val url: String? = null,
    val progress: Float? = null,
    val error: Throwable? = null,
)
