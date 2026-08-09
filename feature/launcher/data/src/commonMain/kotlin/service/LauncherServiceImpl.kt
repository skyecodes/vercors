package app.vercors.feature.launcher.data.service

import app.vercors.core.filesystem.createDirectories
import app.vercors.feature.account.domain.model.Account
import app.vercors.feature.game.domain.model.GameAssetIndex
import app.vercors.feature.game.domain.model.GameVersionDetails
import app.vercors.feature.instance.domain.model.Instance
import app.vercors.feature.launcher.domain.model.InstanceLaunchEvent
import app.vercors.feature.launcher.domain.model.InstanceLaunchFeature
import app.vercors.feature.launcher.domain.service.LaunchRuleEvaluator
import app.vercors.feature.launcher.domain.service.LauncherService
import app.vercors.shared.data.AppPaths
import app.vercors.shared.data.FileValidationFailure
import app.vercors.shared.data.FileValidationService
import arrow.core.NonEmptyList
import arrow.core.raise.Raise
import arrow.core.raise.either
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.io.files.Path
import kotlin.coroutines.CoroutineContext

private val logger = KotlinLogging.logger {}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class LauncherServiceImpl(
    private val appPaths: AppPaths,
    private val ruleEvaluator: LaunchRuleEvaluator,
    private val dependencyResolver: LaunchDependencyResolver,
    private val nativesExtractor: NativesExtractor,
    private val commandBuilder: LaunchCommandBuilder,
    private val fileValidationService: FileValidationService
) : LauncherService {
    override fun launch(
        instance: Instance,
        account: Account,
        version: GameVersionDetails,
        assetIndex: GameAssetIndex,
        features: List<InstanceLaunchFeature>,
    ): Flow<InstanceLaunchEvent> = channelFlow {
        logger.info { "Launching instance ${instance.name}..." }
        val effectiveVersion = ruleEvaluator.applyRules(version, features)
        val dependencyList = dependencyResolver.resolve(instance.id, effectiveVersion, assetIndex)
        either { dependencyList.validate() }.onLeft { failures ->
            logger.error { "Failed to validate dependencies for instance ${instance.name}: ${failures.size} invalid files" }
            logger.debug { "Invalid files: ${failures.joinToString(", ") { it.toString() }}" }
            return@channelFlow
        }
        nativesExtractor.extract(dependencyList.natives)
        val command = commandBuilder.build(dependencyList, account, instance, effectiveVersion)
        val gameDir = appPaths.gameDir(instance.id)
        gameDir.createDirectories()
        val runJob = launch { runCommand(command, gameDir, effectiveVersion.clientLogging?.type) }
        send(InstanceLaunchEvent.Running { runJob.cancel() })
        runJob.join()
        logger.info { "Instance ${instance.name} finished" }
    }

    context(_: ProducerScope<InstanceLaunchEvent>, _: Raise<NonEmptyList<FileValidationFailure>>)
    private suspend fun InstanceDependencyList.validate() {
        logger.info { "Validating dependencies..." }
        fileValidationService.submit(allDependencies) {

        }
        logger.info { "Dependencies validated" }
    }
}

internal expect val platformPathSeparator: String

internal expect suspend fun ProducerScope<InstanceLaunchEvent>.runCommand(
    command: List<String>,
    workingDir: Path,
    loggingType: GameVersionDetails.Logging.Type?,
    context: CoroutineContext = Dispatchers.IO
)
