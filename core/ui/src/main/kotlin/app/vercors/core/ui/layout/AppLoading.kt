package app.vercors.core.ui.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.vercors.core.ui.CoreRes
import app.vercors.core.ui.component.AppText
import app.vercors.core.ui.loading
import org.jetbrains.compose.resources.stringResource

@Composable
fun AppLoading() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AppText(
            text = stringResource(CoreRes.string.loading),
        )
    }
}
