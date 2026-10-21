package app.vercors.feature.config.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.vercors.core.ui.component.AppText
import app.vercors.core.ui.theme.colors
import app.vercors.core.ui.theme.label
import app.vercors.core.ui.theme.onBackground
import app.vercors.core.ui.theme.textStyles
import com.composeunstyled.theme.Theme
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ConfigurationRow(
    title: StringResource,
    description: StringResource,
    requiresRestart: Boolean = false,
    content: @Composable RowScope.() -> Unit
) = ConfigurationRow(stringResource(title), stringResource(description), requiresRestart, content)

@Composable
internal fun ConfigurationRow(
    title: String,
    description: String,
    requiresRestart: Boolean = false,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            AppText(text = title, fontWeight = FontWeight.SemiBold)
            AppText(text = description, style = Theme[textStyles][label])
            if (requiresRestart) {
                AppText(
                    text = stringResource(ConfigRes.string.restartRequired),
                    style = Theme[textStyles][label],
                    color = Theme[colors][onBackground].copy(alpha = 0.6f)
                )
            }
        }
        content()
    }
}
