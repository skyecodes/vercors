package app.vercors.feature.game.data.repository

import app.vercors.core.filesystem.exists
import app.vercors.core.filesystem.realPath
import app.vercors.core.filesystem.setExecutable
import app.vercors.core.system.OsArch
import app.vercors.core.system.OsType
import app.vercors.core.system.SystemService
import app.vercors.feature.config.domain.repository.ConfigurationRepository
import app.vercors.feature.game.api.Game
import app.vercors.feature.game.data.dto.MojangJavaInfo
import app.vercors.feature.game.data.dto.MojangJavaManifest
import app.vercors.feature.game.data.service.probeJavaExecutable
import app.vercors.feature.game.data.service.toJavaMajorVersion
import app.vercors.feature.game.domain.model.JavaRuntimeError
import app.vercors.feature.game.domain.model.JavaRuntimeInstallStatus
import app.vercors.feature.game.domain.repository.JavaRuntimeRepository
import app.vercors.shared.api.ApiV1
import app.vercors.shared.data.AppPaths
import app.vercors.shared.data.FileValidationRequest
import app.vercors.shared.data.FileValidationService
import app.vercors.shared.domain.model.JavaInstallation
import arrow.core.raise.Raise
import arrow.core.raise.context.raise
import arrow.core.raise.context.withError
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.io.files.Path
import kotlinx.serialization.serializer
import opensavvy.progress.Progress
import opensavvy.spine.api.div
import opensavvy.spine.client.arrow.body
import opensavvy.spine.client.request

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class JavaRuntimeRepositoryImpl(
    private val fileValidationService: FileValidationService,
    private val systemService: SystemService,
    private val appPaths: AppPaths,
    private val configurationRepository: ConfigurationRepository,
) : JavaRuntimeRepository {
    private val _installProgress = MutableStateFlow<Map<Int, JavaRuntimeInstallStatus>>(emptyMap())
    override val installProgress: StateFlow<Map<Int, JavaRuntimeInstallStatus>> = _installProgress

    private val manifestRequest = FileValidationRequest.Channel(
        channel = { it.request(ApiV1 / Game / Game.runtimes).body() },
        path = appPaths.javaRuntimeManifestFile,
        alwaysRedownload = true,
    )

    context(_: Raise<JavaRuntimeError>)
    override suspend fun installRuntime(majorVersion: Int): JavaInstallation {
        try {
            val manifest = withError(JavaRuntimeError::InvalidManifest) {
                fileValidationService.submitAndRead(manifestRequest, serializer<MojangJavaManifest>())
            }
            val osArchKey = systemService.mojangJavaRuntimeKey()
            val (component, entry) = manifest[osArchKey].orEmpty().entries
                .mapNotNull { (component, entries) -> entries.firstOrNull()?.let { component to it } }
                .firstOrNull { (_, entry) -> entry.version.name.toJavaMajorVersion() == majorVersion }
                ?: raise(JavaRuntimeError.ComponentNotAvailable(majorVersion))

            val runtimeInfoRequest = FileValidationRequest.Url(
                path = Path(appPaths.downloadsTmp, "java-runtime-$component.json"),
                url = entry.manifest.url,
                sha1 = entry.manifest.sha1,
                size = entry.manifest.size,
                alwaysRedownload = true,
            )
            val runtimeInfo = withError(JavaRuntimeError::InvalidManifest) {
                fileValidationService.submitAndRead(runtimeInfoRequest, MojangJavaInfo.serializer())
            }

            val runtimeDir = appPaths.javaRuntimeDir(component)
            val normalFiles = runtimeInfo.files.mapNotNull { (relativePath, file) ->
                if (file is MojangJavaInfo.File.Normal) relativePath to file else null
            }
            val fileRequests = normalFiles.map { (relativePath, file) ->
                FileValidationRequest.Url(
                    path = relativePath.toRuntimeFilePath(runtimeDir),
                    url = file.downloads.raw.url,
                    sha1 = file.downloads.raw.sha1,
                    size = file.downloads.raw.size,
                )
            }
            withError(JavaRuntimeError::DownloadFailed) {
                fileValidationService.submit(fileRequests) { progress ->
                    _installProgress.update { it + (majorVersion to JavaRuntimeInstallStatus.Downloading(progress.toFractionOrNull())) }
                }
            }
            normalFiles.filter { (_, file) -> file.executable }.forEach { (relativePath, _) ->
                relativePath.toRuntimeFilePath(runtimeDir).setExecutable()
            }

            val javaBinary = listOf(Path(runtimeDir, "bin", "java"), Path(runtimeDir, "bin", "java.exe"))
                .firstOrNull { it.exists() } ?: raise(JavaRuntimeError.ProbeFailed)
            val javaBinaryPath = (javaBinary.realPath() ?: javaBinary).toString()
            val probe = probeJavaExecutable(javaBinaryPath) ?: raise(JavaRuntimeError.ProbeFailed)
            val installation = JavaInstallation(
                id = javaBinaryPath,
                path = javaBinaryPath,
                majorVersion = probe.majorVersion,
                versionString = probe.versionString,
                vendor = probe.vendor,
                arch = probe.arch,
                isManuallyAdded = false,
            )
            configurationRepository.update { mergeJavaInstallations(listOf(installation)) }
            _installProgress.update { it + (majorVersion to JavaRuntimeInstallStatus.Done) }
            return installation
        } finally {
            _installProgress.update { it - majorVersion }
        }
    }

    private fun String.toRuntimeFilePath(runtimeDir: Path): Path = Path(runtimeDir, *split("/").toTypedArray())
}

private fun SystemService.mojangJavaRuntimeKey(): String = when (osType) {
    OsType.Windows -> if (osArch == OsArch.X86) "windows-x86" else "windows-x64"
    OsType.Linux -> if (osArch == OsArch.X86) "linux-i386" else "linux"
    // macOS is Apple Silicon-only in this project (shared.gradle.kts only declares macosArm64()).
    OsType.MacOs -> "mac-os-arm64"
}

private fun Progress.toFractionOrNull(): Float? = when (this) {
    is Progress.Loading.Quantified -> normalized.toFloat()
    Progress.Done -> 1f
    Progress.Loading.Unquantified -> null
}
