package app.vercors.core.ui.modifier

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

fun Modifier.horizontalMouseWheelScroll(state: ScrollState): Modifier = composed {
    val scope = rememberCoroutineScope()
    val pxPerNotch = with(LocalDensity.current) { 64.dp.toPx() }
    pointerInput(state) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent()
                if (event.type == PointerEventType.Scroll) {
                    val scrollDelta = event.changes.first().scrollDelta
                    val amount = if (scrollDelta.x != 0f) scrollDelta.x else scrollDelta.y
                    if (amount != 0f) {
                        scope.launch { state.animateScrollBy(amount * pxPerNotch) }
                        event.changes.forEach { it.consume() }
                    }
                }
            }
        }
    }
}
