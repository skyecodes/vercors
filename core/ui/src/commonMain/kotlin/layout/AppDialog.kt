package app.vercors.core.ui.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import app.vercors.core.ui.component.AppText
import app.vercors.core.ui.modifier.appCard

@Composable
fun AppDialog(onClickOutside: () -> Unit, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            // Deliberately a raw tap-gesture detector rather than Modifier.clickable: clickable makes this scrim
            // focusable and wires up Enter/Space as keyboard-activated clicks, and since key events bubble from
            // whatever's currently focused up through this ancestor, pressing space while typing in a field inside
            // the dialog (e.g. the instance name field) would trigger onClickOutside and close the dialog.
            .pointerInput(onClickOutside) {
                detectTapGestures(onTap = { onClickOutside() })
            },
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
