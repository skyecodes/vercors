package app.vercors.launcher.common.domain.account

import app.vercors.launcher.common.domain.account.model.AccountId

class AccountNotFoundException(accountId: AccountId) :
    IllegalArgumentException("Account with ID '$accountId' not found")
