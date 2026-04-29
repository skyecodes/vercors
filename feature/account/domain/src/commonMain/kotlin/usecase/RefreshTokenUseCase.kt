package app.vercors.feature.account.domain.usecase

import app.vercors.feature.account.domain.repository.AccountRepository
import app.vercors.feature.account.domain.service.AuthenticationService
import app.vercors.feature.account.domain.service.LoginState
import dev.zacsweers.metro.Inject
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach

private val logger = KotlinLogging.logger { }

@Inject
class RefreshTokenUseCase(
    val accountRepository: AccountRepository,
    val authService: AuthenticationService
) {
    operator fun invoke(refreshToken: String): Flow<LoginState> = flow {
        try {
            logger.info { "Refreshing token" }
            authService.refreshToken(refreshToken).onEach {
                if (it is LoginState.Success) accountRepository.addAccount(it.account)
            }.collect(this)
        } catch (e: Exception) {
            currentCoroutineContext().ensureActive()
            emit(LoginState.Error(e))
        }
    }
}
