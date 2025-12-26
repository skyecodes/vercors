package app.vercors.shared.ui.core.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.vercors.shared.ui.core.component.AppIcon
import app.vercors.shared.ui.core.component.AppText
import app.vercors.shared.ui.core.theme.colors
import app.vercors.shared.ui.core.theme.error
import com.composables.icons.lucide.CircleX
import com.composables.icons.lucide.Lucide
import com.composeunstyled.ProvideContentColor
import com.composeunstyled.theme.Theme

@Composable
fun AppError(throwable: Throwable) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        ProvideContentColor(Theme[colors][error]) {
            AppIcon(
                imageVector = Lucide.CircleX,
                contentDescription = throwable.message ?: "An error occurred",
                modifier = Modifier.size(64.dp)
            )
            AppText(("Error: ${throwable.message ?: "An error occurred"}"))
        }
    }
}
