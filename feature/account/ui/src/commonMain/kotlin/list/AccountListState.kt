package app.vercors.feature.account.ui.list

import androidx.compose.runtime.Immutable
import app.vercors.feature.account.domain.model.AccountId
import app.vercors.feature.account.domain.model.AccountLoadingError
import app.vercors.feature.account.domain.model.AccountSkin
import opensavvy.state.progressive.ProgressiveOutcome

@Immutable
data class AccountListState(
    val accountList: ProgressiveOutcome<AccountLoadingError, AccountListView> = ProgressiveOutcome.Incomplete(),
    val loginState: LoginStateView = LoginStateView(),
)

@Immutable
data class AccountListView(
    val accounts: List<AccountView>,
) {
    val selectedAccount: AccountView? = accounts.firstOrNull()
    val otherAccounts: List<AccountView> = accounts.drop(1)
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
