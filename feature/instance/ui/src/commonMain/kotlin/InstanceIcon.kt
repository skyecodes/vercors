package app.vercors.feature.instance.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import okio.Path.Companion.toPath
import kotlin.time.Instant

/**
 * A custom instance icon is always written to the same on-disk path (`icon.<ext>`, replaced in place when the
 * user picks a new one), so Coil's path-keyed cache can't tell a replacement apart from the previous icon —
 * without an explicit cache key that changes on replacement, a freshly set icon keeps showing the old bitmap.
 * [updatedAt] (bumped whenever the icon changes) is folded into the cache key to force a reload.
 */
@Composable
fun rememberInstanceIconPainter(iconFilePath: String, updatedAt: Instant): Painter {
    val cacheKey = "$iconFilePath-$updatedAt"
    return rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalPlatformContext.current)
            .data(iconFilePath.toPath())
            .memoryCacheKey(cacheKey)
            .diskCacheKey(cacheKey)
            .build()
    )
}
