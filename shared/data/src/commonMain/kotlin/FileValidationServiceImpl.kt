package app.vercors.shared.data

import app.vercors.core.cryptography.CryptographyService
import app.vercors.core.filesystem.*
import app.vercors.core.network.RemoteError
import app.vercors.core.network.makeMetaApiRequest
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
import kotlinx.io.bytestring.toHexString
import kotlinx.io.files.Path
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.Json
import opensavvy.progress.Progress
import opensavvy.progress.coroutines.report
import opensavvy.progress.coroutines.reportProgress
import opensavvy.progress.done
import opensavvy.progress.loading
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.incrementAndFetch

private const val MAX_ATTEMPTS = 3L
private const val MAX_PARALLEL_DOWNLOADS = 10

private val logger = KotlinLogging.logger {}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class FileValidationServiceImpl(
    private val cryptographyService: CryptographyService,
    private val json: Json,
    private val httpClient: HttpClient,
) : FileValidationService {
    @OptIn(ExperimentalRaiseAccumulateApi::class, ExperimentalAtomicApi::class)
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

        report(loading(0.0))
        Schedule.recurs<NonEmptyList<FileValidationFailure>>(n = MAX_ATTEMPTS).retryRaise {
            val total = remainingFiles.size.toDouble()
            val progress = AtomicInt(0)
            val results = remainingFiles.parMap(concurrency = MAX_PARALLEL_DOWNLOADS) { request ->
                either { request.apply { downloadAndValidate() } }.also {
                    report(loading(progress.incrementAndFetch() / total))
                }
            }

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
        return file.path.readJson(serializer, json)
    }

    context(_: Raise<FileValidationFailure>)
    override suspend fun <T> readCached(
        file: FileValidationRequest,
        serializer: DeserializationStrategy<T>
    ): T {
        file.validate()
        return file.path.readJson(serializer, json)
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

    context(_: Raise<RemoteError>)
    override suspend fun FileValidationRequest.download() {
        // A blank URL means this file can only ever be produced locally (e.g. a mod loader installer's
        // patched jar) — there's nothing to fetch.
        ensure(this !is FileValidationRequest.Url || url.isNotBlank()) { RemoteError.Network }
        logger.trace { "Downloading file $this" }
        // Download to a sibling temp file and only replace the existing copy once the download has fully
        // succeeded — a failed request (e.g. offline) must never destroy a previously cached, still-valid file.
        val tempPath = Path("$path.tmp")
        try {
            if (this is FileValidationRequest.Channel) {
                makeMetaApiRequest {
                    source.request(httpClient).use {
                        tempPath.writeFromSource(it)
                    }
                }
            } else {
                makeRequest {
                    source.request(httpClient).use {
                        tempPath.writeFromSource(it)
                    }
                }
            }
            ensure((tempPath.metadataOrNull()?.size ?: 0) > 0) { RemoteError.Network }
            tempPath.moveTo(path)
        } finally {
            if (tempPath.exists()) tempPath.deleteRecursively()
        }
    }

    context(_: Raise<FileValidationFailure>)
    override suspend fun FileValidationRequest.downloadAndValidate() {
        either { download() }.mapLeft { FileValidationFailure.DownloadFailed(path, it) }.bind()
        validate()
    }
}
