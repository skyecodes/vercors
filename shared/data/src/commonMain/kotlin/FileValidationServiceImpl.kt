package app.vercors.shared.data

import app.vercors.core.cryptography.CryptographyService
import app.vercors.core.filesystem.*
import app.vercors.core.network.MetaApiConnectivity
import app.vercors.core.network.RemoteError
import app.vercors.core.network.makeRequest
import arrow.core.NonEmptyList
import arrow.core.raise.ExperimentalRaiseAccumulateApi
import arrow.core.raise.Raise
import arrow.core.raise.context.*
import arrow.core.raise.either
import arrow.fx.coroutines.parMap
import arrow.resilience.Schedule
import arrow.resilience.retryRaise
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.io.Source
import kotlinx.io.bytestring.toHexString
import kotlinx.io.files.Path
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import opensavvy.progress.Progress
import opensavvy.progress.coroutines.report
import opensavvy.progress.coroutines.reportProgress
import opensavvy.progress.done

private const val MAX_ATTEMPTS = 3L
private const val MAX_PARALLEL_DOWNLOADS = 10

private val logger = KotlinLogging.logger {}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class FileValidationServiceImpl(
    private val cryptographyService: CryptographyService,
    private val json: Json,
    private val httpClient: HttpClient,
    private val metaApiConnectivity: MetaApiConnectivity,
    private val appPaths: AppPaths,
) : FileValidationService {
    @OptIn(ExperimentalRaiseAccumulateApi::class)
    context(_: Raise<NonEmptyList<FileValidationFailure>>)
    override suspend fun submit(
        files: List<FileValidationRequest>,
        onProgress: (Progress) -> Unit
    ) = reportProgress(onProgress) {
        val remainingFiles = files.filter { either { it.validate(firstTime = true) }.isLeft() }.toMutableList()
        if (remainingFiles.isEmpty()) {
            report(done())
            return@reportProgress
        }

        Schedule.recurs<NonEmptyList<FileValidationFailure>>(n = MAX_ATTEMPTS).retryRaise {
            val tracker = DownloadProgressTracker(
                totalBytes = remainingFiles.sumOfKnownSizes(),
                totalFiles = remainingFiles.size,
                onProgress = onProgress,
            )
            tracker.reportNow()
            val results = remainingFiles.parMap(concurrency = MAX_PARALLEL_DOWNLOADS) { request ->
                either { request.apply { downloadAndValidate(tracker::addBytes) } }
                    .onRight { tracker.completeFile() }
            }
            tracker.reportNow()

            val successes = results.mapNotNull { it.getOrNull() }
            remainingFiles.removeAll(successes)

            accumulate { results.bindAll() }
        }.bind()

        report(done())
    }

    context(_: Raise<FileValidationFailure>)
    override suspend fun <T> submitAndRead(
        file: FileValidationRequest,
        serializer: DeserializationStrategy<T>
    ): T {
        either { submit(listOf(file)) {} }.mapLeft { it.first() }.bind()
        return file.path.readValidatedJson(serializer)
    }

    context(_: Raise<FileValidationFailure>)
    override suspend fun <T> readCached(
        file: FileValidationRequest,
        serializer: DeserializationStrategy<T>
    ): T {
        file.validate()
        return file.path.readValidatedJson(serializer)
    }

    context(_: Raise<FileValidationFailure>)
    private suspend fun <T> Path.readValidatedJson(serializer: DeserializationStrategy<T>): T = try {
        readJson(serializer, json)
    } catch (e: SerializationException) {
        raise(FileValidationFailure.InvalidContent(this, e.message))
    }

    context(_: Raise<FileValidationFailure>)
    override suspend fun FileValidationRequest.validate(firstTime: Boolean) {
        logger.trace { "Validating file $path" }
        ensure(!alwaysRedownload || !firstTime) { FileValidationFailure.FileNotFound(path) }
        val metadata = ensureNotNull(path.metadataOrNull()) { FileValidationFailure.FileNotFound(path) }
        ensure(metadata.isRegularFile) { FileValidationFailure.NotRegularFile(path) }
        if (size != null) ensure(size == metadata.size) { FileValidationFailure.SizeMismatch(path) }
        when {
            sha1 != null -> ensure(sha1 == path.source { cryptographyService.sha1(it) }
                .toHexString()) { FileValidationFailure.HashMismatch(path) }

            md5 != null -> ensure(md5 == path.source { cryptographyService.md5(it) }
                .toHexString()) { FileValidationFailure.HashMismatch(path) }
        }
    }

    // Only sha1 keys the cache: md5-only providers aren't strong enough to share files
    private fun FileValidationRequest.cacheObjectPath(): Path? {
        val kind = cacheKind ?: return null
        val hash = sha1 ?: return null
        return appPaths.contentCacheObjectFile(kind, hash)
    }

    context(_: Raise<RemoteError>)
    override suspend fun FileValidationRequest.download() = download(onBytes = {})

    context(_: Raise<RemoteError>)
    private suspend fun FileValidationRequest.download(onBytes: (Long) -> Unit) {
        // A blank URL means the file is only ever produced locally (e.g. a loader installer's patched jar)
        ensure(this !is FileValidationRequest.Url || url.isNotBlank()) { RemoteError.Network }
        val cacheObject = cacheObjectPath()
        if (cacheObject != null && cacheObject.exists()) {
            // createLink fails on an existing target and would silently fall back to a copy, so delete first
            if (path.exists()) path.delete()
            if (cacheObject.linkOrCopyTo(path)) {
                logger.trace { "Served $path from content cache ($cacheObject)" }
                size?.let(onBytes)
                return
            }
            logger.warn { "Content cache hit for $path but linking from $cacheObject failed; falling back to network" }
        }
        logger.trace { "Downloading file $this" }
        // Download to a temp sibling so a failed request never destroys a valid cached copy
        val tempPath = Path("$path.tmp")
        try {
            if (this is FileValidationRequest.Channel) {
                metaApiConnectivity.request {
                    source.request(httpClient).use {
                        tempPath.writeFromSource(it, onBytes)
                    }
                }
            } else {
                makeRequest {
                    source.request(httpClient).use {
                        when (compression) {
                            FileCompression.Lzma -> tempPath.writeFromLzmaSource(it, onBytes)
                            null -> tempPath.writeFromSource(it, onBytes)
                        }
                    }
                }
            }
            ensure((tempPath.metadataOrNull()?.size ?: 0) > 0) { RemoteError.Network }
            tempPath.moveTo(path)
        } finally {
            if (tempPath.exists()) tempPath.deleteRecursively()
        }
    }

    context(_: Raise<RemoteError>)
    private suspend fun Path.writeFromLzmaSource(source: Source, onBytes: (Long) -> Unit) {
        createParentDirectories()
        val target = this
        withContext(Dispatchers.IO) {
            try {
                target.sink { decodeLzma(source, it, onBytes) }
            } catch (_: LzmaDecodingException) {
                raise(RemoteError.Serialization)
            }
        }
    }

    override suspend fun FileValidationRequest.populateCache() {
        val cacheObject = cacheObjectPath() ?: return
        if (cacheObject.exists()) return
        path.linkOrCopyTo(cacheObject)
    }

    context(_: Raise<FileValidationFailure>)
    override suspend fun FileValidationRequest.downloadAndValidate() = downloadAndValidate(onBytes = {})

    context(_: Raise<FileValidationFailure>)
    private suspend fun FileValidationRequest.downloadAndValidate(onBytes: (Long) -> Unit) {
        either { download(onBytes) }.mapLeft { FileValidationFailure.DownloadFailed(path, it) }.bind()
        validate()
        populateCache()
    }
}

private fun List<FileValidationRequest>.sumOfKnownSizes(): Long? {
    var total = 0L
    for (request in this) total += request.size ?: return null
    return total
}
