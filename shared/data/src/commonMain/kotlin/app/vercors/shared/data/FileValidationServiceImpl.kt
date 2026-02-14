package app.vercors.shared.data

import app.vercors.core.cryptography.CryptographyService
import app.vercors.core.filesystem.*
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
import kotlinx.io.bytestring.toHexString
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.Json

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
    @OptIn(ExperimentalRaiseAccumulateApi::class)
    context(_: Raise<NonEmptyList<FileValidationFailure>>)
    override suspend fun submit(
        files: List<FileValidationRequest>
    ) {
        val remainingFiles = files.filter { either { it.validate() }.isLeft() }.toMutableList()
        if (remainingFiles.isEmpty()) return

        Schedule.recurs<NonEmptyList<FileValidationFailure>>(n = MAX_ATTEMPTS).retryRaise {
            val results = remainingFiles.parMap(concurrency = MAX_PARALLEL_DOWNLOADS) { request ->
                either { request.apply { downloadAndValidate() } }
            }

            val successes = results.mapNotNull { it.getOrNull() }
            remainingFiles.removeAll(successes)

            accumulate { results.bindAll() }
        }.bind()
    }

    context(_: Raise<FileValidationFailure>)
    override suspend fun <T> submitAndRead(
        file: FileValidationRequest,
        serializer: DeserializationStrategy<T>
    ): T {
        either { submit(listOf(file)) }.mapLeft { it.first() }.bind()
        return file.path.readJson(serializer, json)
    }

    context(_: Raise<FileValidationFailure>)
    override suspend fun FileValidationRequest.validate() {
        logger.trace { "Validating file $path" }
        val metadata = ensureNotNull(path.metadataOrNull()) { FileValidationFailure.FileNotFound(path) }
        ensure(metadata.isRegularFile) { FileValidationFailure.NotRegularFile(path) }
        if (size != null) ensure(size == metadata.size) { FileValidationFailure.SizeMismatch(path) }
        if (sha1 != null) ensure(sha1 == path.source { cryptographyService.sha1(it) }
            .toHexString()) { FileValidationFailure.HashMismatch(path) }
    }

    context(_: Raise<RemoteError>)
    override suspend fun FileValidationRequest.download() {
        logger.trace { "Downloading file $this" }
        if (path.exists()) path.deleteRecursively()
        makeRequest {
            source.request(httpClient).use {
                path.writeFromSource(it)
            }
        }
    }

    context(_: Raise<FileValidationFailure>)
    override suspend fun FileValidationRequest.downloadAndValidate() {
        either { download() }.mapLeft { FileValidationFailure.DownloadFailed(path, it) }.bind()
        validate()
    }
}
