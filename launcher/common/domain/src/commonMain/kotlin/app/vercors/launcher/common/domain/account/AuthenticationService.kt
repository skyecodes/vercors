package app.vercors.launcher.common.domain.account

import app.vercors.launcher.common.domain.account.model.LoginState
import kotlinx.coroutines.flow.Flow

interface AuthenticationService {
    fun login(): Flow<LoginState>
    suspend fun verifyToken(accessToken: String): Boolean
    fun refreshToken(refreshToken: String): Flow<LoginState>
}
