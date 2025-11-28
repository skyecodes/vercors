package app.vercors.launcher.common.domain.account.usecase

import app.vercors.launcher.common.domain.account.AccountRepository
import app.vercors.launcher.common.domain.account.AuthenticationService
import app.vercors.launcher.common.domain.account.model.TokenValidationState
import app.vercors.shared.domain.awaitValueOrThrow
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
class ValidateTokenUseCase(
    private val authService: AuthenticationService,
    private val accountRepository: AccountRepository,
    private val loginUseCase: LoginUseCase,
    private val refreshTokenUseCase: RefreshTokenUseCase
) {
    operator fun invoke(): Flow<TokenValidationState> = flow {
        try {
            val account = accountRepository.state.awaitValueOrThrow().selectedAccount
            if (account == null) {
                loginUseCase().map { TokenValidationState.RequireFullLogin(it.throwIfError()) }.collect(this)
            } else {
                val isValid = authService.verifyToken(account.accessToken)
                if (isValid) {
                    emit(TokenValidationState.Success(account.accessToken))
                } else {
                    refreshTokenUseCase(account.refreshToken).map { TokenValidationState.Refreshing(it.throwIfError()) }
                        .collect(this)
                }
            }
        } catch (e: Exception) {
            currentCoroutineContext().ensureActive()
            emit(TokenValidationState.Error(e))
        }
    }
}