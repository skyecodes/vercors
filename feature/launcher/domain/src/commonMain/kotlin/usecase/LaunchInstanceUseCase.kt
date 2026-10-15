package app.vercors.feature.launcher.domain.usecase

import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.model.InstanceStatus
import app.vercors.feature.instance.domain.repository.InstanceRepository
import app.vercors.feature.launcher.domain.model.InstanceLaunchError
import app.vercors.feature.launcher.domain.model.InstanceLaunchEvent
import app.vercors.feature.launcher.domain.model.InstanceLaunchFeature
import app.vercors.feature.launcher.domain.model.QuickPlayTarget
import app.vercors.feature.launcher.domain.service.LauncherService
import arrow.core.raise.DelicateRaiseApi
import arrow.core.raise.Raise
import arrow.core.raise.RaiseCancellationException
import dev.zacsweers.metro.Inject
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch

private val logger = KotlinLogging.logger {}

@Inject
class LaunchInstanceUseCase(
    private val instanceRepository: InstanceRepository,
    private val prepareInstanceLaunch: PrepareInstanceLaunchUseCase,
    private val trackPlaytime: TrackPlaytimeUseCase,
    private val launcherService: LauncherService,
    private val coroutineScope: CoroutineScope,
) {
    @OptIn(DelicateRaiseApi::class)
    context(_: Raise<InstanceLaunchError>)
    suspend operator fun invoke(instanceId: InstanceId, quickPlayTarget: QuickPlayTarget? = null) {
        val job = coroutineScope.async { prepareInstanceLaunch(instanceId) }
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
}
