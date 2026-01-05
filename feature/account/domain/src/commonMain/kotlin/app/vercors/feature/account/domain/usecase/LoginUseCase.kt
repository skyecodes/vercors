package app.vercors.feature.account.domain.usecase

import app.vercors.feature.account.domain.repository.AccountRepository
import app.vercors.feature.account.domain.service.AuthenticationService
import app.vercors.feature.account.domain.service.LoginState
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach

@Inject
class LoginUseCase(
    private val accountRepository: AccountRepository,
    private val authService: AuthenticationService
) {
    operator fun invoke(): Flow<LoginState> = flow {
        try {
            authService.login().onEach {
                if (it is LoginState.Success) accountRepository.addAccount(it.account)
            }.collect(this)
        } catch (e: Exception) {
            currentCoroutineContext().ensureActive()
            emit(LoginState.Error(e))
        }
    }
}
