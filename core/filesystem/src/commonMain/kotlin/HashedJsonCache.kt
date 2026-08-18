package app.vercors.core.filesystem

import kotlinx.io.files.Path
import kotlinx.serialization.KSerializer

/** A global, content-addressed on-disk cache: one JSON file per key, no reactive flow, just point get/put. */
interface HashedJsonCache<T> {
    suspend fun get(hash: String): T?

    suspend fun put(hash: String, value: T)
}

interface HashedJsonCacheFactory {
    operator fun <T> invoke(directoryPath: Path, serializer: KSerializer<T>): HashedJsonCache<T>
}
