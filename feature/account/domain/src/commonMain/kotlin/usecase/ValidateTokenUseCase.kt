package app.vercors.feature.account.domain.usecase

import app.vercors.feature.account.domain.model.Account
import app.vercors.feature.account.domain.service.AuthenticationService
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

@Inject
class ValidateTokenUseCase(
    private val authService: AuthenticationService
) {
    suspend operator fun invoke(account: Account): TokenValidationResult {
        try {
            val isValid = authService.verifyToken(account.accessToken)
            currentCoroutineContext().ensureActive()
            return if (isValid) {
                TokenValidationResult.Success(account.accessToken)
            } else {
                TokenValidationResult.Failure
            }
        } catch (e: Exception) {
            currentCoroutineContext().ensureActive()
            return TokenValidationResult.Error(e)
        }
    }
}

sealed interface TokenValidationResult {
    data class Success(val token: String) : TokenValidationResult
    data object Failure : TokenValidationResult
    data class Error(val error: Exception) : TokenValidationResult
}
