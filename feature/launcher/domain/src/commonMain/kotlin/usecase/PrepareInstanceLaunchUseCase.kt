package app.vercors.feature.launcher.domain.usecase

import app.vercors.feature.game.domain.repository.GameRepository
import app.vercors.feature.game.domain.usecase.ResolveModLoaderUseCase
import app.vercors.feature.instance.domain.model.Instance
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.model.InstanceLogEvent
import app.vercors.feature.instance.domain.model.InstanceStatus
import app.vercors.feature.instance.domain.repository.InstanceRepository
import app.vercors.feature.instance.domain.usecase.ApplyDefaultOptionsUseCase
import app.vercors.feature.java.domain.usecase.ResolveJavaInstallationUseCase
import app.vercors.feature.launcher.domain.model.InstanceLaunchError
import app.vercors.feature.launcher.domain.model.InstanceLaunchPreparation
import arrow.core.getOrElse
import arrow.core.raise.Raise
import arrow.core.raise.context.either
import arrow.core.raise.context.raise
import arrow.core.raise.context.withError
import dev.zacsweers.metro.Inject
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

// Every raise must reset the instance status first: nothing else does, so it would stay on its Launching label
@Inject
class PrepareInstanceLaunchUseCase(
    private val instanceRepository: InstanceRepository,
    private val resolveLaunchAccount: ResolveLaunchAccountUseCase,
    private val gameRepository: GameRepository,
    private val resolveModLoader: ResolveModLoaderUseCase,
    private val resolveJavaInstallation: ResolveJavaInstallationUseCase,
    private val resolveJvmMemoryOptions: ResolveJvmMemoryOptionsUseCase,
    private val resolveNativeLibraryOptions: ResolveNativeLibraryOptionsUseCase,
    private val applyDefaultOptions: ApplyDefaultOptionsUseCase,
) {
    context(_: Raise<InstanceLaunchError>)
    suspend operator fun invoke(instanceId: InstanceId): InstanceLaunchPreparation? {
        logger.info { "Launching instance: $instanceId" }
        val instance = withError({ InstanceLaunchError.InstanceNotFound(it.id) }) {
            instanceRepository.findInstance(instanceId)
        }
        instanceRepository.clearInstanceLogs(instanceId)

        val account = resolveLaunchAccount(instanceId) ?: return null

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

        return InstanceLaunchPreparation(
            instance,
            account,
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
                        InstanceLogEvent.Launcher("Default keybinds skipped: unknown keybind format for this game version")
                    )
                }
            }
    }
}
