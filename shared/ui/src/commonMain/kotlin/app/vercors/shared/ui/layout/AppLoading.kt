package app.vercors.shared.ui.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.vercors.shared.ui.component.AppText
import app.vercors.shared.ui.res.Res
import app.vercors.shared.ui.res.loading
import org.jetbrains.compose.resources.stringResource

@Composable
fun AppLoading() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AppText(
            text = stringResource(Res.string.loading),
        )
    }
}
