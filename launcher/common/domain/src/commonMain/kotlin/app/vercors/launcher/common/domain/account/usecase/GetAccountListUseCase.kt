package app.vercors.launcher.common.domain.account.usecase

import app.vercors.launcher.common.domain.account.AccountRepository
import app.vercors.launcher.common.domain.account.model.AccountList
import app.vercors.shared.domain.ResourceStateFlow
import org.koin.core.annotation.Factory

@Factory
class GetAccountListUseCase(
    private val accountRepository: AccountRepository
) {
    operator fun invoke(): ResourceStateFlow<AccountList> = accountRepository.state
}