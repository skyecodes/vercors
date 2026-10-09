package app.vercors.feature.instance.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.vercors.core.ui.component.AppIconTextButton
import app.vercors.core.ui.theme.*
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Play
import com.composables.icons.lucide.Square
import com.composables.icons.lucide.X
import com.composeunstyled.theme.Theme
import org.jetbrains.compose.resources.stringResource

@Composable
fun InstanceLaunchButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    AppIconTextButton(
        onClick = onClick,
        text = stringResource(InstanceRes.string.launch),
        backgroundColor = Theme[colors][success],
        contentColor = Theme[colors][onSuccess],
        imageVector = Lucide.Play,
        modifier = modifier,
    )
}

@Composable
fun InstanceStopButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    AppIconTextButton(
        onClick = onClick,
        text = stringResource(InstanceRes.string.stop),
        backgroundColor = Theme[colors][error],
        contentColor = Theme[colors][onError],
        imageVector = Lucide.Square,
        modifier = modifier,
    )
}

@Composable
fun InstanceKillButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    AppIconTextButton(
        onClick = onClick,
        text = stringResource(InstanceRes.string.kill),
        backgroundColor = Theme[colors][error],
        contentColor = Theme[colors][onError],
        imageVector = Lucide.X,
        modifier = modifier,
    )
}
