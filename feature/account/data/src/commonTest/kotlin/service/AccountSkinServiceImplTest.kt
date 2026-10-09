package app.vercors.feature.account.data.service

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.uuid.Uuid

class AccountSkinServiceImplTest {
    @Test
    fun `skin urls point at the meta api account endpoints`() {
        val accountId = Uuid.parse("069a79f4-44e9-4726-a5be-fca90e38aaf5")
        val skin = AccountSkinServiceImpl("https://meta.vercors.app").getAccountSkin(accountId)

        assertEquals("https://meta.vercors.app/v1/account/069a79f444e94726a5befca90e38aaf5/avatar", skin.avatarUrl)
        assertEquals("https://meta.vercors.app/v1/account/069a79f444e94726a5befca90e38aaf5/head", skin.headUrl)
        assertEquals("https://meta.vercors.app/v1/account/069a79f444e94726a5befca90e38aaf5/body", skin.bodyUrl)
    }
}
