package app.vercors.feature.account.domain.usecase

import app.vercors.core.domain.await
import app.vercors.core.domain.orThrow
import app.vercors.feature.account.domain.model.Account
import app.vercors.feature.account.domain.model.AccountId
import app.vercors.feature.account.domain.repository.AccountRepository
import dev.zacsweers.metro.Inject
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

@Inject
class LogoutUseCase(private val accountRepository: AccountRepository) {
    suspend operator fun invoke(accountId: AccountId): Account {
        val accountList = accountRepository.state.await().orThrow()
        val account =
            requireNotNull(accountList.accounts.firstOrNull { it.id == accountId }) { "Account with UUID '$accountId' not found" }
        accountRepository.removeAccount(account)
        logger.info { "Logged out of ${account.username} (${account.id.toHexString()})" }
        return account
    }
}
