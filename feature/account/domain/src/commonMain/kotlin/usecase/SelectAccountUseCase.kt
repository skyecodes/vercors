package app.vercors.feature.account.domain.usecase

import app.vercors.feature.account.domain.model.AccountError
import app.vercors.feature.account.domain.model.AccountId
import app.vercors.feature.account.domain.repository.AccountRepository
import arrow.core.raise.Raise
import dev.zacsweers.metro.Inject
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

@Inject
class SelectAccountUseCase(
    private val accountRepository: AccountRepository
) {
    context(_: Raise<AccountError>)
    suspend operator fun invoke(accountId: AccountId) {
        val account = accountRepository.findAccount(accountId)
        accountRepository.selectAccount(account)
        logger.info { "Selected account ${account.username} (${account.id})" }
    }
}
