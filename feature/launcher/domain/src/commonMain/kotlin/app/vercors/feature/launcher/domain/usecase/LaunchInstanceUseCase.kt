package app.vercors.feature.launcher.domain.usecase

import app.vercors.feature.account.domain.repository.AccountRepository
import app.vercors.feature.account.domain.service.LoginState
import app.vercors.feature.account.domain.usecase.RefreshTokenUseCase
import app.vercors.feature.account.domain.usecase.TokenValidationResult
import app.vercors.feature.account.domain.usecase.ValidateTokenUseCase
import app.vercors.feature.game.domain.repository.GameRepository
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.model.InstanceLogEvent
import app.vercors.feature.instance.domain.model.InstanceStatus
import app.vercors.feature.instance.domain.repository.InstanceRepository
import app.vercors.feature.launcher.domain.model.InstanceLaunchError
import app.vercors.feature.launcher.domain.model.InstanceLaunchEvent
import app.vercors.feature.launcher.domain.service.LauncherService
import arrow.core.raise.Raise
import arrow.core.raise.context.withError
import dev.zacsweers.metro.Inject
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.flow.onEach

private val logger = KotlinLogging.logger {}

@Inject
class LaunchInstanceUseCase(
    private val instanceRepository: InstanceRepository,
    private val accountRepository: AccountRepository,
    private val validateTokenUseCase: ValidateTokenUseCase,
    private val refreshTokenUseCase: RefreshTokenUseCase,
    private val gameRepository: GameRepository,
    private val launcherService: LauncherService,
    private val coroutineScope: CoroutineScope,
) {
    context(_: Raise<InstanceLaunchError>)
    suspend operator fun invoke(instanceId: InstanceId) {
        val job = coroutineScope.launch {
            launchInstance(instanceId)
        }
        instanceRepository.updateInstanceJob(instanceId, job)
        job.join()
    }

    context(_: Raise<InstanceLaunchError>)
    private suspend fun launchInstance(instanceId: InstanceId) {
        logger.info { "Launching instance: $instanceId" }
        val instance = withError({ InstanceLaunchError.InstanceNotFound(it.id) }) {
            instanceRepository.findInstance(instanceId)
        }
        instanceRepository.clearInstanceLogs(instanceId)

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
            return
        }

        logger.info { "Selected account: ${account.username} (${account.id})" }
        instanceRepository.updateInstanceStatus(instanceId, InstanceStatus.Launching.ValidatingToken)

        val finalAccount = when (val validateTokenResult = validateTokenUseCase(account)) {
            is TokenValidationResult.Error -> {
                logger.error(validateTokenResult.error) { "Token validation error" }
                instanceRepository.addInstanceLog(
                    instanceId,
                    InstanceLogEvent.Launcher("Token validation error, see logs for more details")
                )
                instanceRepository.updateInstanceStatus(
                    instanceId,
                    InstanceStatus.Failed.TokenValidation(validateTokenResult.error)
                )
                return
            }

            TokenValidationResult.Failure -> {
                logger.info { "Token validation failure, refreshing token" }
                instanceRepository.addInstanceLog(
                    instanceId,
                    InstanceLogEvent.Launcher("Token validation failure, refreshing token")
                )
                instanceRepository.updateInstanceStatus(instanceId, InstanceStatus.Launching.RefreshingToken())
                val refreshTokenResult = refreshTokenUseCase(account.refreshToken).onEach {
                    when (it) {
                        is LoginState.Error -> {
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

                        is LoginState.Progress -> instanceRepository.updateInstanceStatus(
                            instanceId,
                            InstanceStatus.Launching.RefreshingToken(it.step.progress)
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
                if (refreshTokenResult is LoginState.Success)
                    refreshTokenResult.account
                else
                    return
            }

            is TokenValidationResult.Success -> account
        }

        logger.info { "Loading instance metadata" }
        instanceRepository.updateInstanceStatus(instanceId, InstanceStatus.Launching.LoadingMetadata)
        val version = withError({ InstanceLaunchError.GameVersion(it) }) {
            gameRepository.getVersionDetails(instance.gameVersion)
        }
        val assetIndex = withError({ InstanceLaunchError.GameVersion(it) }) {
            gameRepository.getAssetIndex(version.assetIndex)
        }

        launcherService.launch(
            instance = instance,
            account = finalAccount,
            version = version,
            assetIndex = assetIndex,
            features = emptyList()
        ).catch {
            when (it) {
                is CancellationException -> {
                    instanceRepository.updateInstanceStatus(instanceId, InstanceStatus.Idle)
                    currentCoroutineContext().ensureActive()
                }

                is Exception -> {
                    logger.error(it) { "Unknown error" }
                    instanceRepository.updateInstanceStatus(instanceId, InstanceStatus.Failed.Unknown(it))
                }

                else -> throw it
            }
        }.collect {
            when (it) {
                is InstanceLaunchEvent.Validating -> instanceRepository.updateInstanceStatus(
                    instanceId,
                    InstanceStatus.Launching.ValidatingFiles(it.progress)
                )

                is InstanceLaunchEvent.Downloading -> instanceRepository.updateInstanceStatus(
                    instanceId,
                    InstanceStatus.Launching.DownloadingFiles(it.progress)
                )

                InstanceLaunchEvent.Running -> instanceRepository.updateInstanceStatus(
                    instanceId,
                    InstanceStatus.Running
                )

                is InstanceLaunchEvent.Logging -> instanceRepository.addInstanceLog(instanceId, it.logEvent)
            }
        }
    }
}
