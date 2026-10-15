package app.vercors.feature.launcher.domain.usecase

import app.vercors.feature.account.domain.model.Account
import app.vercors.feature.account.domain.repository.AccountRepository
import app.vercors.feature.account.domain.service.AuthServerUnreachableException
import app.vercors.feature.account.domain.service.LoginState
import app.vercors.feature.account.domain.usecase.RefreshTokenUseCase
import app.vercors.feature.account.domain.usecase.TokenValidationResult
import app.vercors.feature.account.domain.usecase.ValidateTokenUseCase
import app.vercors.feature.game.domain.model.GameAssetIndex
import app.vercors.feature.game.domain.model.GameVersionDetails
import app.vercors.feature.game.domain.repository.GameRepository
import app.vercors.feature.game.domain.usecase.ResolveModLoaderUseCase
import app.vercors.feature.instance.domain.model.Instance
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.model.InstanceLogEvent
import app.vercors.feature.instance.domain.model.InstanceStatus
import app.vercors.feature.instance.domain.repository.InstanceRepository
import app.vercors.feature.instance.domain.usecase.ApplyDefaultOptionsUseCase
import app.vercors.feature.instance.domain.usecase.UpdateInstanceUseCase
import app.vercors.feature.java.domain.usecase.ResolveJavaInstallationUseCase
import app.vercors.feature.launcher.domain.model.InstanceLaunchError
import app.vercors.feature.launcher.domain.model.InstanceLaunchEvent
import app.vercors.feature.launcher.domain.model.InstanceLaunchFeature
import app.vercors.feature.launcher.domain.model.QuickPlayTarget
import app.vercors.feature.launcher.domain.service.LauncherService
import app.vercors.shared.domain.model.JvmMemoryOptions
import app.vercors.shared.domain.model.NativeLibraryOptions
import arrow.core.getOrElse
import arrow.core.raise.DelicateRaiseApi
import arrow.core.raise.Raise
import arrow.core.raise.RaiseCancellationException
import arrow.core.raise.context.either
import arrow.core.raise.context.raise
import arrow.core.raise.context.withError
import dev.zacsweers.metro.Inject
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.flow.onEach
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

private val logger = KotlinLogging.logger {}

private val PLAYTIME_TICK_INTERVAL = 30.seconds

@Inject
class LaunchInstanceUseCase(
    private val instanceRepository: InstanceRepository,
    private val accountRepository: AccountRepository,
    private val validateTokenUseCase: ValidateTokenUseCase,
    private val refreshTokenUseCase: RefreshTokenUseCase,
    private val gameRepository: GameRepository,
    private val resolveModLoader: ResolveModLoaderUseCase,
    private val resolveJavaInstallation: ResolveJavaInstallationUseCase,
    private val resolveJvmMemoryOptions: ResolveJvmMemoryOptionsUseCase,
    private val resolveNativeLibraryOptions: ResolveNativeLibraryOptionsUseCase,
    private val launcherService: LauncherService,
    private val updateInstance: UpdateInstanceUseCase,
    private val applyDefaultOptions: ApplyDefaultOptionsUseCase,
    private val coroutineScope: CoroutineScope,
) {
    @OptIn(DelicateRaiseApi::class)
    context(_: Raise<InstanceLaunchError>)
    suspend operator fun invoke(instanceId: InstanceId, quickPlayTarget: QuickPlayTarget? = null) {
        val job = coroutineScope.async { prepareInstance(instanceId) }
        job.invokeOnCompletion { instanceRepository.updateInstanceJob(instanceId, null) }
        instanceRepository.updateInstanceJob(instanceId, job)
        val (instance, account, version, assetIndex, javaExecutable, jvmMemoryOptions, nativeLibraryOptions) = try {
            job.await()
        } catch (e: RaiseCancellationException) {
            // raise() short-circuits via a CancellationException subtype: rethrow it so it reaches its Raise scope
            throw e
        } catch (_: CancellationException) {
            instanceRepository.updateInstanceStatus(instanceId, InstanceStatus.Idle)
            currentCoroutineContext().ensureActive()
            return
        } ?: return

        val features = when (quickPlayTarget) {
            is QuickPlayTarget.Singleplayer -> listOf(InstanceLaunchFeature.IsQuickPlaySingleplayer)
            is QuickPlayTarget.Multiplayer -> listOf(InstanceLaunchFeature.IsQuickPlayMultiplayer)
            null -> emptyList()
        }

        var playtimeJob: Job? = null
        try {
            launcherService.launch(
                instance = instance,
                account = account,
                version = version,
                assetIndex = assetIndex,
                features = features,
                javaExecutable = javaExecutable,
                jvmMemoryOptions = jvmMemoryOptions,
                nativeLibraryOptions = nativeLibraryOptions,
                quickPlayTarget = quickPlayTarget
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
            }.collect { event ->
                onLaunchEvent(event, instanceId)
                if (event is InstanceLaunchEvent.Running) {
                    playtimeJob = coroutineScope.launch { trackPlaytime(instanceId, instance.playTime) }
                }
            }
        } finally {
            playtimeJob?.cancelAndJoin()
        }

        instanceRepository.updateInstanceStatus(instanceId, InstanceStatus.Idle)
    }

    // Recomputes the total from the session baseline, so a delayed or duplicated tick never double-counts
    private suspend fun trackPlaytime(instanceId: InstanceId, baselinePlayTime: Duration) {
        val sessionStart = Clock.System.now()
        try {
            persistPlaytime(instanceId, baselinePlayTime, sessionStart)
            while (true) {
                delay(PLAYTIME_TICK_INTERVAL)
                persistPlaytime(instanceId, baselinePlayTime, sessionStart)
            }
        } finally {
            withContext(NonCancellable) { persistPlaytime(instanceId, baselinePlayTime, sessionStart) }
        }
    }

    private suspend fun persistPlaytime(instanceId: InstanceId, baselinePlayTime: Duration, sessionStart: Instant) {
        val now = Clock.System.now()
        either {
            updateInstance(instanceId) {
                it.copy(
                    playTime = baselinePlayTime + (now - sessionStart),
                    lastPlayedAt = now
                )
            }
        }.onLeft { error -> logger.warn { "Failed to persist playtime for $instanceId: $error" } }
    }

    context(_: Raise<InstanceLaunchError>)
    private suspend fun prepareInstance(instanceId: InstanceId): InstancePreparationResult? {
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
            raise(InstanceLaunchError.LoginRequired)
        }

        logger.info { "Selected account: ${account.username} (${account.id})" }
        instanceRepository.updateInstanceStatus(instanceId, InstanceStatus.Launching.ValidatingToken)

        val finalAccount = when (val validateTokenResult = validateTokenUseCase(account)) {
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
                val refreshTokenResult = refreshTokenUseCase(account.refreshToken).onEach {
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

        logger.info { "Loading instance metadata" }
        instanceRepository.updateInstanceStatus(instanceId, InstanceStatus.Launching.LoadingMetadata)
        val vanillaVersion = either { gameRepository.getVersionDetails(instance.gameVersion) }.getOrElse {
            instanceRepository.updateInstanceStatus(instanceId, InstanceStatus.Idle)
            raise(InstanceLaunchError.GameVersion(it))
        }
        val version = instance.modLoader?.let { modLoader ->
            logger.info { "Installing ${modLoader.type} ${modLoader.version}..." }
            instanceRepository.updateInstanceStatus(
                instanceId,
                InstanceStatus.Launching.InstallingLoader(modLoader.type)
            )
            either { resolveModLoader(vanillaVersion, modLoader.type, modLoader.version) }
                .getOrElse {
                    instanceRepository.updateInstanceStatus(instanceId, InstanceStatus.Idle)
                    raise(InstanceLaunchError.ModLoaderInstall(it))
                }
        } ?: vanillaVersion
        val assetIndex = either { gameRepository.getAssetIndex(version.assetIndex) }.getOrElse {
            instanceRepository.updateInstanceStatus(instanceId, InstanceStatus.Idle)
            raise(InstanceLaunchError.GameVersion(it))
        }
        val javaExecutable = version.javaVersion?.let { javaVersion ->
            resolveJavaInstallation(javaVersion.majorVersion, instance.javaInstallationId)?.path
                ?: run {
                    instanceRepository.updateInstanceStatus(instanceId, InstanceStatus.Idle)
                    raise(InstanceLaunchError.NoCompatibleJava(javaVersion.majorVersion))
                }
        } ?: "java"
        val jvmMemoryOptions = resolveJvmMemoryOptions(instance.jvmMemoryOptions)
        val nativeLibraryOptions = resolveNativeLibraryOptions(instance.nativeLibraryOptions)
        seedDefaultOptions(instance)

        return InstancePreparationResult(
            instance,
            finalAccount,
            version,
            assetIndex,
            javaExecutable,
            jvmMemoryOptions,
            nativeLibraryOptions
        )
    }

    private suspend fun seedDefaultOptions(instance: Instance) {
        either { applyDefaultOptions(instance) }
            .onLeft { logger.warn { "Could not apply default options to ${instance.id}: $it" } }
            .onRight { result ->
                if (result?.skippedCategories?.isNotEmpty() == true) {
                    instanceRepository.addInstanceLog(
                        instance.id,
                        InstanceLogEvent.Launcher("Default keybinds skipped: incompatible with this game version")
                    )
                }
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

    private fun onLaunchEvent(event: InstanceLaunchEvent, instanceId: InstanceId) {
        when (event) {
            is InstanceLaunchEvent.Validating -> instanceRepository.updateInstanceStatus(
                instanceId,
                InstanceStatus.Launching.ValidatingFiles(event.progress)
            )

            is InstanceLaunchEvent.Downloading -> instanceRepository.updateInstanceStatus(
                instanceId,
                InstanceStatus.Launching.DownloadingFiles(event.download)
            )

            is InstanceLaunchEvent.Running -> instanceRepository.updateInstanceStatus(
                instanceId,
                InstanceStatus.Running(stop = event.stop)
            )

            is InstanceLaunchEvent.Stopping -> instanceRepository.updateInstanceStatus(
                instanceId,
                InstanceStatus.Stopping(kill = event.kill)
            )

            is InstanceLaunchEvent.Logging -> instanceRepository.addInstanceLog(instanceId, event.logEvent)
        }
    }

    private data class InstancePreparationResult(
        val instance: Instance,
        val account: Account,
        val version: GameVersionDetails,
        val assetIndex: GameAssetIndex,
        val javaExecutable: String,
        val jvmMemoryOptions: JvmMemoryOptions,
        val nativeLibraryOptions: NativeLibraryOptions,
    )
}
