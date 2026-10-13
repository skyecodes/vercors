package app.vercors.core.ui.layout

import androidx.compose.foundation.*
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
fun AppDialog(
    modifier: Modifier = Modifier,
    onClickOutside: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    val scrollState = rememberScrollState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .pointerInput(onClickOutside) {
                detectTapGestures(onTap = { onClickOutside() })
            }
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = modifier
                .appCard()
                .clickable(enabled = false) {}
        ) {
            Box(modifier = Modifier.verticalScroll(scrollState).padding(16.dp)) {
                content()
            }
            if (scrollState.canScrollBackward || scrollState.canScrollForward) {
                Box(modifier = Modifier.matchParentSize()) {
                    VerticalScrollbar(
                        adapter = rememberScrollbarAdapter(scrollState),
                        modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight().padding(vertical = 8.dp)
                    )
                }
            }
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
    AppDialog(
        modifier = Modifier.widthIn(max = 600.dp),
        onClickOutside = onClickOutside
    ) {
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
