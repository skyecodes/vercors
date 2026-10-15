package app.vercors.launcher.gui.update

import app.vercors.core.domain.AppConstants
import app.vercors.core.domain.DownloadProgress
import app.vercors.core.network.makeRequest
import app.vercors.feature.config.domain.model.LauncherUpdateError
import app.vercors.feature.config.domain.model.LauncherUpdateState
import app.vercors.feature.config.domain.model.UpdateChannel
import app.vercors.feature.config.domain.service.LauncherUpdateService
import arrow.core.raise.either
import dev.nucleusframework.updater.NucleusUpdater
import dev.nucleusframework.updater.UpdateInfo
import dev.nucleusframework.updater.UpdateResult
import dev.nucleusframework.updater.Version
import dev.nucleusframework.updater.provider.GitHubProvider
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import java.io.File

private val logger = KotlinLogging.logger {}

private const val GITHUB_OWNER = "skyecodes"
private const val GITHUB_REPO = "vercors"
private const val GITHUB_RELEASES_URL = "https://github.com/$GITHUB_OWNER/$GITHUB_REPO/releases"

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class NucleusLauncherUpdateService(
    private val httpClient: HttpClient,
) : LauncherUpdateService {
    private val updaters = mutableMapOf<UpdateChannel, NucleusUpdater>()
    private val mutex = Mutex()
    private val _state = MutableStateFlow<LauncherUpdateState>(LauncherUpdateState.Idle)
    private var pendingUpdate: Pair<NucleusUpdater, UpdateInfo>? = null
    private var downloadedUpdate: Pair<NucleusUpdater, File>? = null

    override val state: StateFlow<LauncherUpdateState> = _state.asStateFlow()

    override suspend fun check(channel: UpdateChannel) {
        if (!mutex.tryLock()) return
        try {
            pendingUpdate = null
            downloadedUpdate = null
            _state.value = LauncherUpdateState.Checking
            val updater = updaterFor(channel)
            _state.value = if (updater.isUpdateSupported()) {
                checkWithUpdater(updater)
            } else {
                checkReleases(channel, updater.currentVersion)
            }
        } finally {
            mutex.unlock()
        }
    }

    override suspend fun download() {
        val (updater, info) = pendingUpdate ?: return
        if (!mutex.tryLock()) return
        try {
            val releaseUrl = releaseUrlOf(info.version)
            _state.value = LauncherUpdateState.Downloading(info.version, releaseUrl, download = null)
            var file: File? = null
            updater.downloadUpdate(info).collect { progress ->
                _state.value = LauncherUpdateState.Downloading(
                    version = info.version,
                    releaseUrl = releaseUrl,
                    download = DownloadProgress(
                        downloadedBytes = progress.bytesDownloaded,
                        totalBytes = progress.totalBytes.takeIf { it > 0 },
                        completedFiles = if (progress.file != null) 1 else 0,
                        totalFiles = 1,
                    )
                )
                progress.file?.let { file = it }
            }
            _state.value = file?.let {
                downloadedUpdate = updater to it
                LauncherUpdateState.ReadyToInstall(info.version, releaseUrl)
            } ?: LauncherUpdateState.Failed(LauncherUpdateError.DownloadFailed("No file was downloaded"))
        } catch (e: Exception) {
            currentCoroutineContext().ensureActive()
            logger.error(e) { "Failed to download launcher update ${info.version}" }
            _state.value = LauncherUpdateState.Failed(LauncherUpdateError.DownloadFailed(e.message ?: e.toString()))
        } finally {
            mutex.unlock()
        }
    }

    override suspend fun installAndRestart() {
        val (updater, file) = downloadedUpdate ?: return
        val readyState = _state.value as? LauncherUpdateState.ReadyToInstall ?: return
        if (!mutex.tryLock()) return
        try {
            _state.value = LauncherUpdateState.Restarting(readyState.version, readyState.releaseUrl)
            logger.info { "Installing launcher update from $file" }
            // Blocks while the platform installer runs, then exits the process
            withContext(Dispatchers.IO) { updater.installAndRestart(file) }
            _state.value = readyState
        } catch (e: Exception) {
            currentCoroutineContext().ensureActive()
            logger.error(e) { "Failed to install launcher update ${readyState.version}" }
            _state.value = LauncherUpdateState.Failed(LauncherUpdateError.InstallFailed(e.message ?: e.toString()))
        } finally {
            mutex.unlock()
        }
    }

    // An updater's config is frozen at construction, so each channel needs its own instance
    private fun updaterFor(channel: UpdateChannel): NucleusUpdater = updaters.getOrPut(channel) {
        NucleusUpdater {
            provider = GitHubProvider(owner = GITHUB_OWNER, repo = GITHUB_REPO)
            this.channel = channel.feedName
        }
    }

    private suspend fun checkWithUpdater(updater: NucleusUpdater): LauncherUpdateState =
        when (val result = updater.checkForUpdates()) {
            is UpdateResult.Available -> {
                pendingUpdate = updater to result.info
                LauncherUpdateState.Available(
                    version = result.info.version,
                    releaseUrl = releaseUrlOf(result.info.version),
                    canInstall = true
                )
            }

            UpdateResult.NotAvailable -> LauncherUpdateState.UpToDate
            is UpdateResult.Error -> {
                logger.error(result.exception) { "Failed to check for launcher updates" }
                LauncherUpdateState.Failed(LauncherUpdateError.CheckFailed(result.exception.message ?: "Unknown error"))
            }
        }

    // Nucleus refuses to check for formats it can't install (pacman, flatpak, snap, dev runs), so look up releases directly
    private suspend fun checkReleases(channel: UpdateChannel, currentVersion: String): LauncherUpdateState {
        // Same rule as Nucleus: a pre-release build keeps following its own channel until a newer stable release
        val effectiveChannel =
            if (channel == UpdateChannel.Stable) UpdateChannel.fromVersion(currentVersion) else channel
        val remoteVersion = when (effectiveChannel) {
            UpdateChannel.Stable -> fetchLatestStableVersion()
            UpdateChannel.Beta, UpdateChannel.Alpha -> fetchLatestPrereleaseVersion(effectiveChannel)
        } ?: return LauncherUpdateState.Failed(LauncherUpdateError.CheckFailed("Could not read the published releases"))
        val isNewer = runCatching {
            Version.fromString(remoteVersion) > Version.fromString(currentVersion)
        }.getOrElse {
            logger.warn(it) { "Cannot compare launcher versions $remoteVersion and $currentVersion" }
            false
        }
        return if (isNewer) {
            LauncherUpdateState.Available(remoteVersion, releaseUrlOf(remoteVersion), canInstall = false)
        } else {
            LauncherUpdateState.UpToDate
        }
    }

    private suspend fun fetchLatestStableVersion(): String? =
        fetchText("$GITHUB_RELEASES_URL/latest/download/${stableFeedFileName()}")
            ?.let { FEED_VERSION_REGEX.find(it)?.groupValues?.get(1)?.trim('\'', '"') }

    private suspend fun fetchLatestPrereleaseVersion(channel: UpdateChannel): String? =
        fetchText("$GITHUB_RELEASES_URL.atom")
            ?.let { feed -> RELEASE_TAG_REGEX.findAll(feed).map { it.groupValues[1].removePrefix("v") }.toSet() }
            ?.filter { channel.follows(prereleaseNameOf(it)) }
            ?.mapNotNull { tag -> runCatching { Version.fromString(tag) to tag }.getOrNull() }
            ?.maxByOrNull { it.first }
            ?.second

    private suspend fun fetchText(url: String): String? =
        either { makeRequest { httpClient.get(url).bodyAsText() } }
            .onLeft { logger.warn { "Failed to fetch $url: $it" } }
            .getOrNull()

    private fun stableFeedFileName(): String {
        val os = System.getProperty("os.name").lowercase()
        return when {
            os.startsWith("windows") -> "latest.yml"
            os.startsWith("mac") -> "latest-mac.yml"
            else -> "latest-linux.yml"
        }
    }

    private fun releaseUrlOf(version: String): String = "${AppConstants.REPOSITORY_URL}/releases/tag/v$version"
}

private val UpdateChannel.feedName: String
    get() = when (this) {
        UpdateChannel.Stable -> "latest"
        UpdateChannel.Beta -> "beta"
        UpdateChannel.Alpha -> "alpha"
    }

private fun UpdateChannel.follows(prerelease: String?): Boolean = when (this) {
    UpdateChannel.Stable -> prerelease == null
    UpdateChannel.Beta -> prerelease == null || prerelease == "beta"
    UpdateChannel.Alpha -> prerelease == null || prerelease == "beta" || prerelease == "alpha"
}

private fun prereleaseNameOf(version: String): String? =
    version.substringAfter('-', missingDelimiterValue = "").substringBefore('.').ifEmpty { null }

private val FEED_VERSION_REGEX = Regex("""(?m)^version:\s*(\S+)""")
private val RELEASE_TAG_REGEX = Regex("""/releases/tag/([^"<\s]+)""")
