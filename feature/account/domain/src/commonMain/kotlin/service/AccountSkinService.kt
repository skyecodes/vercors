package app.vercors.feature.account.domain.service

import app.vercors.feature.account.domain.model.AccountId
import app.vercors.feature.account.domain.model.AccountSkin

fun interface AccountSkinService {
    fun getAccountSkin(accountId: AccountId): AccountSkin
}
