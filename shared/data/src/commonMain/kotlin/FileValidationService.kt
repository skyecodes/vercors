package app.vercors.shared.data

import app.vercors.core.network.RemoteError
import app.vercors.core.network.bodyAsSource
import arrow.core.NonEmptyList
import arrow.core.raise.Raise
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.utils.io.*
import kotlinx.io.Source
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.serialization.DeserializationStrategy
import opensavvy.progress.Progress

interface FileValidationService {
    context(_: Raise<NonEmptyList<FileValidationFailure>>)
    suspend fun submit(files: List<FileValidationRequest>, onProgress: (Progress) -> Unit)

    context(_: Raise<FileValidationFailure>)
    suspend fun <T> submitAndRead(file: FileValidationRequest, serializer: DeserializationStrategy<T>): T

    context(_: Raise<FileValidationFailure>)
    suspend fun <T> readCached(file: FileValidationRequest, serializer: DeserializationStrategy<T>): T

    context(_: Raise<FileValidationFailure>)
    suspend fun FileValidationRequest.downloadAndValidate()

    context(_: Raise<FileValidationFailure>)
    suspend fun FileValidationRequest.validate(firstTime: Boolean = false)

    context(_: Raise<RemoteError>)
    suspend fun FileValidationRequest.download()

    /** Hardlinks this request's already-validated file at [FileValidationRequest.path] into the shared content
     *  cache keyed by [FileValidationRequest.cacheKind] + sha1, if not already present there. No-ops when
     *  [FileValidationRequest.cacheKind] is null or [FileValidationRequest.sha1] is null. Callers must only
     *  invoke this once [path]'s contents are known-good (e.g. right after [validate] succeeds) — anything
     *  placed here is later served to *other* installs of the same hash with no re-verification. */
    suspend fun FileValidationRequest.populateCache()
}

interface FileValidationRequest {
    val source: FileRemoteSource
    val path: Path
    val size: Long?
    val sha1: String?
    val md5: String?
    val alwaysRedownload: Boolean

    /** When non-null (and [sha1] is present), this request's file is eligible for the shared content cache: a
     *  cache hit skips the network download entirely, and a cache miss populates the cache after a successful
     *  download for future installs of the same file to hit. */
    val cacheKind: ContentCacheKind?

    open class Url(
        val url: String,
        override val path: Path,
        override val size: Long? = null,
        override val sha1: String? = null,
        override val md5: String? = null,
        override val alwaysRedownload: Boolean = false,
        override val cacheKind: ContentCacheKind? = null
    ) : FileValidationRequest {
        override val source: FileRemoteSource = FileRemoteSource { it.get(url).bodyAsSource() }
        override fun toString(): String = "$url -> $path"
    }

    class Channel(
        channel: suspend (HttpClient) -> ByteReadChannel,
        override val path: Path,
        override val size: Long? = null,
        override val sha1: String? = null,
        override val md5: String? = null,
        override val alwaysRedownload: Boolean = false,
        override val cacheKind: ContentCacheKind? = null
    ) : FileValidationRequest {
        override val source: FileRemoteSource = FileRemoteSource { channel(it).asSource().buffered() }
        override fun toString(): String = "-> $path"
    }
}

fun interface FileRemoteSource {
    suspend fun request(httpClient: HttpClient): Source
}

sealed interface FileValidationFailure {
    val path: Path

    data class DownloadFailed(override val path: Path, val remoteError: RemoteError) : FileValidationFailure
    data class FileNotFound(override val path: Path) : FileValidationFailure
    data class NotRegularFile(override val path: Path) : FileValidationFailure
    data class SizeMismatch(override val path: Path) : FileValidationFailure
    data class HashMismatch(override val path: Path) : FileValidationFailure
}
