package app.vercors.launcher.common.domain.account.usecase

import app.vercors.launcher.common.domain.account.AccountRepository
import app.vercors.launcher.common.domain.account.model.Account
import app.vercors.shared.domain.awaitValueOrThrow
import io.github.oshai.kotlinlogging.KotlinLogging
import org.koin.core.annotation.Factory

private val logger = KotlinLogging.logger {}

@Factory
class LogoutUseCase(private val accountRepository: AccountRepository) {
    suspend operator fun invoke(uuidOrUsername: String): Account {
        val accountList = accountRepository.state.awaitValueOrThrow()
        val account = requireNotNull(accountList.accounts.firstOrNull {
            it.id.toHexString() == uuidOrUsername || it.id.toHexDashString() == uuidOrUsername || it.username == uuidOrUsername
        }) { "Account with UUID or username '$uuidOrUsername' not found" }
        accountRepository.removeAccount(account)
        logger.info { "Logged out of ${account.username} (${account.id.toHexString()})" }
        return account
    }
}
