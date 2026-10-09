package app.vercors.feature.java.data.repository

import app.vercors.core.domain.ProgressiveStateFlow
import app.vercors.core.domain.progressiveStateIn
import app.vercors.core.filesystem.exists
import app.vercors.core.filesystem.realPath
import app.vercors.core.system.SystemService
import app.vercors.feature.config.domain.model.Configuration
import app.vercors.feature.config.domain.model.ConfigurationError
import app.vercors.feature.config.domain.repository.ConfigurationRepository
import app.vercors.feature.java.data.service.probeJavaExecutable
import app.vercors.feature.java.domain.model.JavaInstallationError
import app.vercors.feature.java.domain.model.JavaProbeResult
import app.vercors.feature.java.domain.repository.JavaRepository
import app.vercors.shared.domain.model.JavaInstallation
import arrow.core.raise.Raise
import arrow.core.raise.context.raise
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.files.Path
import opensavvy.state.progressive.ProgressiveOutcome
import opensavvy.state.progressive.successfulWithProgress

private val logger = KotlinLogging.logger {}

internal data class CandidateJavaPath(val path: String)

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class JavaRepositoryImpl(
    private val configurationRepository: ConfigurationRepository,
    private val systemService: SystemService,
    private val backgroundScope: CoroutineScope,
) : JavaRepository {
    private val scanMutex = Mutex()

    override val installations: ProgressiveStateFlow<Nothing, List<JavaInstallation>> =
        configurationRepository.state
            .map { it.toInstallationsOutcome() }
            .onStart { backgroundScope.launch { scanIfNeverScanned() } }
            .progressiveStateIn(backgroundScope, started = SharingStarted.Eagerly)

    private fun ProgressiveOutcome<ConfigurationError, Configuration>.toInstallationsOutcome(): ProgressiveOutcome<Nothing, List<JavaInstallation>> =
        when (this) {
            is ProgressiveOutcome.Success -> value.javaInstallations.successfulWithProgress(progress)
            is ProgressiveOutcome.Incomplete -> ProgressiveOutcome.Incomplete(progress)
            is ProgressiveOutcome.Failure -> emptyList<JavaInstallation>().successfulWithProgress(progress)
        }

    private suspend fun scanIfNeverScanned() {
        if (currentConfiguration()?.javaInstallations.isNullOrEmpty()) rescan()
    }

    private fun currentConfiguration(): Configuration? =
        (configurationRepository.state.value as? ProgressiveOutcome.Success)?.value

    // Waits instead of skipping when a scan is running: short-lived callers like `vercors java scan` need it done
    override suspend fun rescan() {
        scanMutex.withLock {
            logger.debug { "Scanning for Java installations..." }
            val existingIds = currentConfiguration()?.javaInstallations?.map { it.id }?.toSet().orEmpty()
            val rawCandidatePaths = (
                    platformScanJavaInstallationsWindows(systemService) +
                            platformScanJavaInstallationsLinux(systemService) +
                            platformScanJavaInstallationsMacOs(systemService)
                    )
                .map { it.path }
                .distinct()

            // Resolve symlinks before deduping: distros expose the same binary through several paths
            val candidatePaths = coroutineScope {
                rawCandidatePaths.map { path -> async { path.resolveRealPathOrSelf() } }.awaitAll()
            }.distinct().filter { it !in existingIds }
            logger.debug { "Java scan candidate paths: $candidatePaths (from ${rawCandidatePaths.size} raw candidates)" }

            val newInstallations = coroutineScope {
                candidatePaths.map { path ->
                    async {
                        probeJavaExecutable(path)?.let { probe ->
                            path.toInstallation(
                                probe,
                                isManuallyAdded = false
                            )
                        }
                    }
                }.awaitAll().filterNotNull()
            }
            logger.debug { "Java scan found ${newInstallations.size} new installation(s)" }
            if (newInstallations.isNotEmpty()) {
                configurationRepository.update { mergeJavaInstallations(newInstallations) }
            }
        }
    }

    context(_: Raise<JavaInstallationError>)
    override suspend fun addInstallation(path: String): JavaInstallation {
        val binaryPath = resolveJavaBinaryPath(path).resolveRealPathOrSelf()
        val probe = probeJavaExecutable(binaryPath) ?: raise(JavaInstallationError.NotAJavaInstallation(path))
        val installation = binaryPath.toInstallation(probe, isManuallyAdded = true)
        configurationRepository.update { mergeJavaInstallations(listOf(installation)) }
        return installation
    }

    private suspend fun String.resolveRealPathOrSelf(): String = Path(this).realPath()?.toString() ?: this

    private fun String.toInstallation(probe: JavaProbeResult, isManuallyAdded: Boolean) =
        JavaInstallation(
            id = this,
            path = this,
            majorVersion = probe.majorVersion,
            versionString = probe.versionString,
            vendor = probe.vendor,
            arch = probe.arch,
            isManuallyAdded = isManuallyAdded,
        )

    private suspend fun resolveJavaBinaryPath(path: String): String {
        val candidates =
            listOf(Path(path, "bin", "java"), Path(path, "bin", "java.exe"), Path(path, "bin", "javaw.exe"))
        return candidates.firstOrNull { it.exists() }?.toString() ?: path
    }
}
