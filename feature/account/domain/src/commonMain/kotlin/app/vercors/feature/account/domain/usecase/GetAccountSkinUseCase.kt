package app.vercors.feature.account.domain.usecase

import app.vercors.feature.account.domain.model.AccountId
import app.vercors.feature.account.domain.model.AccountSkin
import app.vercors.feature.account.domain.service.AccountSkinService
import org.koin.core.annotation.Factory

@Factory
class GetAccountSkinUseCase(
    private val accountSkinService: AccountSkinService
) {
    operator fun invoke(accountId: AccountId): AccountSkin = accountSkinService.getAccountSkin(accountId)
}
