package app.vercors.feature.account.ui.list

import app.vercors.feature.account.domain.model.AccountId
import app.vercors.shared.ui.Event

sealed interface AccountListEvent : Event {
    data object Login : AccountListEvent
    data object CancelLogin : AccountListEvent
    data class CopyToClipboard(val data: String) : AccountListEvent
    data class Logout(val accountId: AccountId) : AccountListEvent
    data class SelectAccount(val accountId: AccountId) : AccountListEvent
}
