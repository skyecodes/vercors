package app.vercors.launcher.common.domain.account

import app.vercors.launcher.common.domain.account.model.AccountId
import app.vercors.launcher.common.domain.account.model.AccountSkin

fun interface AccountSkinService {
    fun getAccountSkin(accountId: AccountId): AccountSkin
}
