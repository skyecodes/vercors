package app.vercors.launcher.common.domain.account.usecase

import app.vercors.launcher.common.domain.account.AccountRepository
import app.vercors.launcher.common.domain.account.AuthenticationService
import app.vercors.launcher.common.domain.account.model.LoginState
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import org.koin.core.annotation.Factory

@Factory
class RefreshTokenUseCase(
    val accountRepository: AccountRepository,
    val authService: AuthenticationService
) {
    operator fun invoke(refreshToken: String): Flow<LoginState> = flow {
        try {
            authService.refreshToken(refreshToken).onEach {
                if (it is LoginState.Success) accountRepository.addAccount(it.account)
            }.collect(this)
        } catch (e: Exception) {
            currentCoroutineContext().ensureActive()
            emit(LoginState.Error(e))
        }
    }
}