package app.vercors.launcher.shell

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import app.vercors.core.ui.theme.background
import app.vercors.core.ui.theme.backgroundAlt
import app.vercors.core.ui.theme.colors
import com.composeunstyled.theme.Theme
import com.slack.circuit.sharedelements.SharedElementTransitionLayout

@Composable
fun AppFrame(
    topBar: @Composable () -> Unit,
    menu: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxSize().background(Theme[colors][backgroundAlt])
    ) {
        topBar()
        Row {
            menu()
            AppFrameContent(content)
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun AppFrameContent(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(end = 4.dp, top = 4.dp, bottom = 4.dp)
            .background(
                color = Theme[colors][background],
                shape = RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
    ) {
        SharedElementTransitionLayout {
            content()
        }
    }
}
