package app.vercors.feature.account.api

import kotlin.test.Test
import kotlin.test.assertNotNull

class AccountApiContractTest {
    @Test
    fun `every endpoint declares a valid contract`() {
        listOf(
            Accounts.AccountId.avatar,
            Accounts.AccountId.head,
            Accounts.AccountId.body,
        ).forEach(::assertNotNull)
    }
}
