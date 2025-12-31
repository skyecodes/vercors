package app.vercors.feature.account.data.service

import app.vercors.feature.account.domain.model.AccountId
import app.vercors.feature.account.domain.model.AccountSkin
import app.vercors.feature.account.domain.service.AccountSkinService
import org.koin.core.annotation.Property
import org.koin.core.annotation.Single

@Single
class AccountSkinServiceImpl(
    @Property("META_BASE_URL") private val metaBaseUrl: String
) : AccountSkinService {
    override fun getAccountSkin(accountId: AccountId): AccountSkin = accountId.toHexString().let {
        AccountSkin(
            avatarUrl = "${metaBaseUrl}account/${it}/avatar",
            headUrl = "${metaBaseUrl}account/${it}/head",
            bodyUrl = "${metaBaseUrl}account/${it}/body"
        )
    }
}
