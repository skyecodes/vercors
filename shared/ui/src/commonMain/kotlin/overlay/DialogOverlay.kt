package app.vercors.shared.ui.overlay

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import com.slack.circuit.overlay.AnimatedOverlay

abstract class DialogOverlay<Result : Any> : AnimatedOverlay<Result>(
    enterTransition = fadeIn(),
    exitTransition = fadeOut()
)
