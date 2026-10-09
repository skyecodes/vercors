package app.vercors.feature.account.domain.usecase

import app.vercors.feature.account.domain.model.Account
import app.vercors.feature.account.domain.model.AccountError
import app.vercors.feature.account.domain.model.AccountId
import app.vercors.feature.account.domain.repository.AccountRepository
import arrow.core.raise.Raise
import dev.zacsweers.metro.Inject
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

@Inject
class LogoutUseCase(
    private val accountRepository: AccountRepository
) {
    context(_: Raise<AccountError>)
    suspend operator fun invoke(accountId: AccountId): Account {
        val account = accountRepository.findAccount(accountId)
        accountRepository.removeAccount(account)
        logger.info { "Logged out of ${account.username} (${account.id.toHexString()})" }
        return account
    }
}
