package app.vercors.launcher.common.domain.account.usecase

import app.vercors.launcher.common.domain.account.AccountSkinService
import app.vercors.launcher.common.domain.account.model.AccountId
import app.vercors.launcher.common.domain.account.model.AccountSkin
import org.koin.core.annotation.Factory

@Factory
class GetAccountSkinUseCase(
    private val accountSkinService: AccountSkinService
) {
    operator fun invoke(accountId: AccountId): AccountSkin = accountSkinService.getAccountSkin(accountId)
}
