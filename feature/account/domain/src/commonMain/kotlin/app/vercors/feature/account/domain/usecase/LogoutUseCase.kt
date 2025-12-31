package app.vercors.feature.account.domain.usecase

import app.vercors.core.domain.await
import app.vercors.core.domain.orThrow
import app.vercors.feature.account.domain.model.Account
import app.vercors.feature.account.domain.repository.AccountRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.koin.core.annotation.Factory

private val logger = KotlinLogging.logger {}

@Factory
class LogoutUseCase(private val accountRepository: AccountRepository) {
    suspend operator fun invoke(uuidOrUsername: String): Account {
        val accountList = accountRepository.state.await().orThrow()
        val account = requireNotNull(accountList.accounts.firstOrNull {
            it.id.toHexString() == uuidOrUsername || it.id.toHexDashString() == uuidOrUsername || it.username == uuidOrUsername
        }) { "Account with UUID or username '$uuidOrUsername' not found" }
        accountRepository.removeAccount(account)
        logger.info { "Logged out of ${account.username} (${account.id.toHexString()})" }
        return account
    }
}
