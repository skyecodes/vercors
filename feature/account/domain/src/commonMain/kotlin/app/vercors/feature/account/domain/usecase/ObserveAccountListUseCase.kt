package app.vercors.feature.account.domain.usecase

import app.vercors.core.domain.ResourceStateFlow
import app.vercors.feature.account.domain.model.AccountList
import app.vercors.feature.account.domain.repository.AccountRepository
import dev.zacsweers.metro.Inject

@Inject
class ObserveAccountListUseCase(
    private val accountRepository: AccountRepository
) {
    operator fun invoke(): ResourceStateFlow<AccountList> = accountRepository.state
}
