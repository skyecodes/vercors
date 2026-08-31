package app.vercors.feature.project.ui.install

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.vercors.core.ui.component.AppIconTextButton
import app.vercors.core.ui.theme.colors
import app.vercors.core.ui.theme.onPrimary
import app.vercors.core.ui.theme.primary
import app.vercors.feature.project.ui.ProjectRes
import app.vercors.feature.project.ui.install
import com.composables.icons.lucide.Download
import com.composables.icons.lucide.Lucide
import com.composeunstyled.theme.Theme
import org.jetbrains.compose.resources.stringResource

@Composable
fun ProjectInstallButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    AppIconTextButton(
        onClick = onClick,
        text = stringResource(ProjectRes.string.install),
        imageVector = Lucide.Download,
        backgroundColor = Theme[colors][primary],
        contentColor = Theme[colors][onPrimary],
        modifier = modifier,
    )
}
