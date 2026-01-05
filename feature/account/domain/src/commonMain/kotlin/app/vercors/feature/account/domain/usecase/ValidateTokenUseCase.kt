package app.vercors.feature.account.domain.usecase

import app.vercors.feature.account.domain.model.Account
import app.vercors.feature.account.domain.service.AuthenticationService
import app.vercors.feature.account.domain.service.LoginState
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

@Inject
class ValidateTokenUseCase(
    private val authService: AuthenticationService,
    private val refreshTokenUseCase: RefreshTokenUseCase
) {
    operator fun invoke(account: Account): Flow<TokenValidationState> = flow {
        try {
            val isValid = authService.verifyToken(account.accessToken)
            currentCoroutineContext().ensureActive()
            if (isValid) {
                emit(TokenValidationState.Success(account.accessToken))
            } else {
                refreshTokenUseCase(account.refreshToken).map { TokenValidationState.Refreshing(it) }
                    .collect(this)
            }
        } catch (e: Exception) {
            currentCoroutineContext().ensureActive()
            emit(TokenValidationState.Error(e))
        }
    }
}

sealed interface TokenValidationState {
    data class Success(val token: String) : TokenValidationState
    data class Refreshing(val loginState: LoginState) : TokenValidationState
    data class RequireFullLogin(val loginState: LoginState) : TokenValidationState
    data class Error(val error: Exception) : TokenValidationState
}
