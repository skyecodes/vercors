package app.vercors.feature.launcher.domain.usecase

import app.vercors.feature.account.domain.model.Account
import app.vercors.feature.account.domain.repository.AccountRepository
import app.vercors.feature.account.domain.service.AuthServerUnreachableException
import app.vercors.feature.account.domain.service.LoginState
import app.vercors.feature.account.domain.usecase.RefreshTokenUseCase
import app.vercors.feature.account.domain.usecase.TokenValidationResult
import app.vercors.feature.account.domain.usecase.ValidateTokenUseCase
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.model.InstanceLogEvent
import app.vercors.feature.instance.domain.model.InstanceStatus
import app.vercors.feature.instance.domain.repository.InstanceRepository
import app.vercors.feature.launcher.domain.model.InstanceLaunchError
import arrow.core.raise.Raise
import arrow.core.raise.context.raise
import arrow.core.raise.context.withError
import dev.zacsweers.metro.Inject
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.flow.onEach

private val logger = KotlinLogging.logger {}

// Null when the account can't be used: the instance status already says why
@Inject
class ResolveLaunchAccountUseCase(
    private val instanceRepository: InstanceRepository,
    private val accountRepository: AccountRepository,
    private val validateToken: ValidateTokenUseCase,
    private val refreshToken: RefreshTokenUseCase,
) {
    context(_: Raise<InstanceLaunchError>)
    suspend operator fun invoke(instanceId: InstanceId): Account? {
        val account = withError({ InstanceLaunchError.AccountLoading(it.cause) }) {
            accountRepository.getSelectedAccount()
        }
        if (account == null) {
            logger.warn { "No account selected, login required" }
            instanceRepository.addInstanceLog(
                instanceId,
                InstanceLogEvent.Launcher("No account selected, login required")
            )
            instanceRepository.updateInstanceStatus(instanceId, InstanceStatus.Failed.LoginRequired)
            raise(InstanceLaunchError.LoginRequired)
        }

        logger.info { "Selected account: ${account.username} (${account.id})" }
        instanceRepository.updateInstanceStatus(instanceId, InstanceStatus.Launching.ValidatingToken)

        return when (val validateTokenResult = validateToken(account)) {
            is TokenValidationResult.Error -> {
                if (validateTokenResult.error is AuthServerUnreachableException) {
                    offlineFallback(instanceId, account)
                } else {
                    logger.error(validateTokenResult.error) { "Token validation error" }
                    instanceRepository.addInstanceLog(
                        instanceId,
                        InstanceLogEvent.Launcher("Token validation error, see logs for more details")
                    )
                    instanceRepository.updateInstanceStatus(
                        instanceId,
                        InstanceStatus.Failed.TokenValidation(validateTokenResult.error)
                    )
                    return null
                }
            }

            TokenValidationResult.Failure -> {
                logger.info { "Token validation failure, refreshing token" }
                instanceRepository.addInstanceLog(
                    instanceId,
                    InstanceLogEvent.Launcher("Token validation failure, refreshing token")
                )
                instanceRepository.updateInstanceStatus(instanceId, InstanceStatus.Launching.RefreshingToken)
                val refreshTokenResult = refreshToken(account.refreshToken).onEach {
                    when (it) {
                        is LoginState.Error -> {
                            if (it.error !is AuthServerUnreachableException) {
                                logger.error(it.error) { "Token refresh error" }
                                instanceRepository.addInstanceLog(
                                    instanceId,
                                    InstanceLogEvent.Launcher("Token refresh error, see logs for more details")
                                )
                                instanceRepository.updateInstanceStatus(
                                    instanceId,
                                    InstanceStatus.Failed.TokenRefresh(it.error)
                                )
                            }
                        }

                        is LoginState.Progress -> instanceRepository.updateInstanceStatus(
                            instanceId,
                            InstanceStatus.Launching.RefreshingToken
                        )

                        is LoginState.RequireLogin -> {
                            logger.warn { "Unable to refresh token, login required" }
                            instanceRepository.addInstanceLog(
                                instanceId,
                                InstanceLogEvent.Launcher("Unable to refresh token, login required")
                            )
                            instanceRepository.updateInstanceStatus(instanceId, InstanceStatus.Failed.LoginRequired)
                        }

                        is LoginState.Success -> Unit
                    }
                }.lastOrNull()
                logger.info { "Refresh token result: $refreshTokenResult" }
                when {
                    refreshTokenResult is LoginState.Success -> refreshTokenResult.account
                    refreshTokenResult is LoginState.Error && refreshTokenResult.error is AuthServerUnreachableException ->
                        offlineFallback(instanceId, account)

                    else -> return null
                }
            }

            is TokenValidationResult.Success -> account
        }
    }

    private fun offlineFallback(instanceId: InstanceId, account: Account): Account {
        logger.warn { "Could not reach the authentication server, launching offline with the last-known account" }
        instanceRepository.addInstanceLog(
            instanceId,
            InstanceLogEvent.Launcher("Could not reach the authentication server, launching offline")
        )
        return account
    }
}
