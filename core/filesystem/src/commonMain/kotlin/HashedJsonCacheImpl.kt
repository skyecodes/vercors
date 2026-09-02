package app.vercors.core.filesystem

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.files.Path
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

private val logger = KotlinLogging.logger {}

internal class HashedJsonCacheImpl<T>(
    private val directoryPath: Path,
    private val serializer: KSerializer<T>,
    private val json: Json
) : HashedJsonCache<T> {
    private val locks = mutableMapOf<String, Mutex>()

    override suspend fun get(hash: String): T? {
        val filePath = fileFor(hash)
        if (!filePath.exists()) return null
        return try {
            filePath.readJson(serializer, json)
        } catch (e: Exception) {
            currentCoroutineContext().ensureActive()
            if (e is CancellationException) throw e
            logger.error(e) { "Failed to read data from $filePath" }
            null
        }
    }

    override suspend fun put(hash: String, value: T) {
        val filePath = fileFor(hash)
        locks.getOrPut(hash, ::Mutex).withLock {
            filePath.writeJson(serializer, value, json)
        }
    }

    private fun fileFor(hash: String): Path = Path(directoryPath, hash.take(2), "$hash.json")
}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class HashedJsonCacheFactoryImpl(
    private val json: Json
) : HashedJsonCacheFactory {
    override fun <T> invoke(directoryPath: Path, serializer: KSerializer<T>): HashedJsonCache<T> =
        HashedJsonCacheImpl(directoryPath, serializer, json)
}
