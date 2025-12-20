package app.vercors.shared.ui.animation

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.compositionLocalOf

val LocalSharedTransitionScope =
    compositionLocalOf<SharedTransitionScope> { error("No SharedTransitionScope provided") }
