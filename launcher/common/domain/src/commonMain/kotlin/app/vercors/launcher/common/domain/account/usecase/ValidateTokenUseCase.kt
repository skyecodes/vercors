package app.vercors.launcher.common.domain.account.usecase

import app.vercors.launcher.common.domain.account.AuthenticationService
import app.vercors.launcher.common.domain.account.model.Account
import app.vercors.launcher.common.domain.account.model.TokenValidationState
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
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
