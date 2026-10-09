package app.vercors.feature.account.ui.list

import app.vercors.feature.account.domain.model.AccountId
import app.vercors.shared.ui.Event

sealed interface AccountListEvent : Event {
    data object OnLoginClick : AccountListEvent
    data object OnLoginDismiss : AccountListEvent
    data class OnCopyClick(val data: String) : AccountListEvent
    data class OnLogoutClick(val accountId: AccountId) : AccountListEvent
    data class OnAccountClick(val accountId: AccountId) : AccountListEvent
}
