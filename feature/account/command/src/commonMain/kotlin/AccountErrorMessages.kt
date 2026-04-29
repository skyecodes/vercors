package app.vercors.feature.account.command

import app.vercors.feature.account.domain.model.AccountError
import app.vercors.feature.account.domain.model.AccountLoadingError
import app.vercors.feature.account.domain.model.AccountNotFound

val AccountError.message: String
    get() = when (this) {
        is AccountLoadingError -> "Failed to load accounts: $cause"
        is AccountNotFound -> "Account with ID $accountId not found"
    }
