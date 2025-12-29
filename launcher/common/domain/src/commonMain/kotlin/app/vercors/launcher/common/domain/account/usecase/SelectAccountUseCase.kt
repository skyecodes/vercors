package app.vercors.launcher.common.domain.account.usecase

import app.vercors.launcher.common.domain.account.AccountRepository
import app.vercors.launcher.common.domain.account.model.AccountId
import app.vercors.shared.domain.awaitValueOrThrow
import io.github.oshai.kotlinlogging.KotlinLogging
import org.koin.core.annotation.Factory

private val logger = KotlinLogging.logger {}

@Factory
class SelectAccountUseCase(
    private val accountRepository: AccountRepository
) {
    suspend operator fun invoke(accountId: AccountId) {
        val accountList = accountRepository.state.awaitValueOrThrow()
        val account = accountList.accounts.find { it.id == accountId }
            ?: throw IllegalArgumentException("Account with ID $accountId not found")
        accountRepository.selectAccount(account)
        logger.info { "Selected account ${account.username} (${account.id})" }
    }
}
