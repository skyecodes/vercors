package app.vercors.feature.config.data.repository

import app.vercors.core.filesystem.deleteRecursively
import app.vercors.core.filesystem.exists
import app.vercors.core.filesystem.sizeRecursively
import app.vercors.feature.config.domain.repository.ContentCacheRepository
import app.vercors.shared.data.AppPaths
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class ContentCacheRepositoryImpl(
    private val appPaths: AppPaths,
) : ContentCacheRepository {
    override suspend fun sizeBytes(): Long =
        if (appPaths.contentCacheRoot.exists()) appPaths.contentCacheRoot.sizeRecursively() else 0

    override suspend fun clear() {
        if (appPaths.contentCacheRoot.exists()) appPaths.contentCacheRoot.deleteRecursively()
    }
}
