package app.vercors.feature.instance.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.vercors.core.ui.component.AppBadge
import app.vercors.core.ui.component.AppIcon
import app.vercors.core.ui.theme.colors
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.model.InstanceStatus
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import com.composables.icons.lucide.Gamepad2
import com.composables.icons.lucide.Lucide
import com.composeunstyled.theme.Theme
import com.slack.circuit.sharedelements.SharedElementTransitionScope
import okio.Path.Companion.toPath
import kotlin.time.Instant

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

@Composable
context(_: SharedElementTransitionScope)
fun InstanceIcon(
    instanceId: InstanceId,
    name: String,
    iconFilePath: String?,
    updatedAt: Instant,
    status: InstanceStatus,
    size: Dp,
    shape: Shape,
    modifier: Modifier = Modifier,
    origin: InstanceSharedElementOrigin? = null,
) {
    Box(
        modifier = modifier.size(size)
            .instanceSharedElement(instanceId, "icon", origin)
    ) {
        Box(
            modifier = Modifier.matchParentSize()
                .clip(shape)
        ) {
            if (iconFilePath != null) {
                Image(
                    painter = rememberInstanceIconPainter(iconFilePath, updatedAt),
                    contentDescription = name,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                AppIcon(
                    imageVector = Lucide.Gamepad2,
                    contentDescription = name,
                    modifier = Modifier.matchParentSize()
                )
            }
        }
        status.badgeTone?.let { tone ->
            AppBadge(
                color = Theme[colors][tone],
                modifier = Modifier.align(Alignment.TopEnd).offset(x = 2.dp, y = (-2).dp)
            )
        }
    }
}
