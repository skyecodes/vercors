package app.vercors.feature.account.domain.service

import app.vercors.feature.account.domain.model.Account
import kotlinx.coroutines.flow.Flow

interface AuthenticationService {
    fun login(): Flow<LoginState>
    suspend fun verifyToken(accessToken: String): Boolean
    fun refreshToken(refreshToken: String): Flow<LoginState>
}

sealed interface LoginState {
    data class RequireLogin(val url: String) : LoginState
    data class Progress(val step: LoginStep) : LoginState
    data class Success(val account: Account) : LoginState
    data class Error(val error: Exception) : LoginState
}

enum class LoginStep(val number: Int, val description: String) {
    MicrosoftToken(1, "Getting Microsoft access token"),
    XBLToken(2, "Getting XBL token"),
    XSTSToken(3, "Getting XSTS token"),
    MinecraftToken(4, "Getting Minecraft access token"),
    MinecraftProfile(5, "Getting Minecraft profile"),
    Success(6, "Login successful"),
}

open class AuthenticationException(
    message: String? = "An authentication error has occurred",
    cause: Exception? = null
) : Exception(message, cause)
