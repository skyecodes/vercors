package app.vercors.feature.account.domain.model

import kotlin.jvm.JvmInline

sealed interface AccountError

@JvmInline
value class AccountLoadingError(val cause: Any) : AccountError

@JvmInline
value class AccountNotFound(val accountId: AccountId) : AccountError
