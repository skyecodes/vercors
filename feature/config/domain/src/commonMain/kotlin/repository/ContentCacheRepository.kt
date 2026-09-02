package app.vercors.feature.config.domain.repository

interface ContentCacheRepository {
    suspend fun sizeBytes(): Long
    suspend fun clear()
}
