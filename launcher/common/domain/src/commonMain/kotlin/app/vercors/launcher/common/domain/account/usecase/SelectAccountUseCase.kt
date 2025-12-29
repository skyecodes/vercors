package app.vercors.launcher.common.domain.account.usecase

import app.vercors.launcher.common.domain.account.AccountRepository
import app.vercors.launcher.common.domain.account.model.AccountId
import io.github.oshai.kotlinlogging.KotlinLogging
import org.koin.core.annotation.Factory

private val logger = KotlinLogging.logger {}

@Factory
class SelectAccountUseCase(
    private val accountRepository: AccountRepository
) {
    suspend operator fun invoke(accountId: AccountId) {
        val account = accountRepository.findAccount(accountId)
        accountRepository.selectAccount(account)
        logger.info { "Selected account ${account.username} (${account.id})" }
    }
}
