package app.vercors.launcher.common.domain.account.model

sealed interface TokenValidationState {
    data class Success(val token: String) : TokenValidationState
    data class Refreshing(val loginState: LoginState) : TokenValidationState
    data class RequireFullLogin(val loginState: LoginState) : TokenValidationState
    data class Error(val error: Exception) : TokenValidationState
}