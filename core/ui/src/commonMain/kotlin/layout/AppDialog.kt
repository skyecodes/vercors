package app.vercors.core.ui.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.vercors.core.ui.component.AppText
import app.vercors.core.ui.modifier.appCard

@Composable
fun AppDialog(onClickOutside: () -> Unit, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClickOutside
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .appCard()
                .padding(16.dp)
                .clickable(enabled = false) {}
        ) {
            content()
        }
    }
}

@Composable
fun AppConfirmDialog(
    confirmText: String,
    confirmButton: @Composable RowScope.() -> Unit,
    dismissButton: @Composable RowScope.() -> Unit,
    onClickOutside: () -> Unit
) {
    AppDialog(onClickOutside = onClickOutside) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AppText(
                text = confirmText
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                confirmButton()
                dismissButton()
            }
        }
    }
}
