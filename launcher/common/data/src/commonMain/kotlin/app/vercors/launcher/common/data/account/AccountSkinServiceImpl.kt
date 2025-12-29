package app.vercors.launcher.common.data.account

import app.vercors.launcher.common.domain.account.AccountSkinService
import app.vercors.launcher.common.domain.account.model.AccountId
import app.vercors.launcher.common.domain.account.model.AccountSkin
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
