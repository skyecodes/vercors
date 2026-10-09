package app.vercors.feature.account.data.service

import app.vercors.core.domain.AppConstants
import app.vercors.feature.account.api.Accounts
import app.vercors.feature.account.domain.model.AccountId
import app.vercors.feature.account.domain.model.AccountSkin
import app.vercors.feature.account.domain.service.AccountSkinService
import app.vercors.shared.api.ApiV1
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Named
import dev.zacsweers.metro.SingleIn
import opensavvy.spine.api.div
import opensavvy.spine.api.invoke

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class AccountSkinServiceImpl(
    @Named(AppConstants.META_API_URL_KEY) private val metaApiUrl: String
) : AccountSkinService {
    override fun getAccountSkin(accountId: AccountId): AccountSkin {
        val account = ApiV1 / Accounts / Accounts.AccountId(accountId.toHexString())
        return AccountSkin(
            avatarUrl = "$metaApiUrl${(account / Accounts.AccountId.avatar).path}",
            headUrl = "$metaApiUrl${(account / Accounts.AccountId.head).path}",
            bodyUrl = "$metaApiUrl${(account / Accounts.AccountId.body).path}"
        )
    }
}
