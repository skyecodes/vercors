package app.vercors.feature.account.data.service

import app.vercors.feature.account.domain.model.AccountId
import app.vercors.feature.account.domain.model.AccountSkin
import app.vercors.feature.account.domain.service.AccountSkinService
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Named
import dev.zacsweers.metro.SingleIn

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class AccountSkinServiceImpl(
    @Named("metaApiUrl") private val metaApiUrl: String
) : AccountSkinService {
    override fun getAccountSkin(accountId: AccountId): AccountSkin = accountId.toHexString().let {
        AccountSkin(
            avatarUrl = "${metaApiUrl}/v1/account/${it}/avatar",
            headUrl = "${metaApiUrl}/v1/account/${it}/head",
            bodyUrl = "${metaApiUrl}/v1/account/${it}/body"
        )
    }
}
