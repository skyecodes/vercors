package app.vercors.feature.account.domain.usecase

import app.vercors.feature.account.domain.model.Account
import app.vercors.feature.account.domain.model.AccountLoadingError
import app.vercors.feature.account.domain.repository.AccountRepository
import dev.zacsweers.metro.Inject
import opensavvy.state.coroutines.ProgressiveFlow

@Inject
class ObserveAccountListUseCase(
    private val accountRepository: AccountRepository
) {
    operator fun invoke(): ProgressiveFlow<AccountLoadingError, List<Account>> = accountRepository.state
}
