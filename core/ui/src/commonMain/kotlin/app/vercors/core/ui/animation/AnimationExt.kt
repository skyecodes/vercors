package app.vercors.core.ui.animation

import androidx.compose.animation.*
import androidx.compose.animation.SharedTransitionScope.*
import androidx.compose.animation.SharedTransitionScope.PlaceholderSize.Companion.ContentSize
import androidx.compose.animation.SharedTransitionScope.ResizeMode.Companion.scaleToBounds
import androidx.compose.animation.core.*
import androidx.compose.animation.core.Spring.StiffnessMediumLow
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.*

val LocalAnimation = compositionLocalOf { true }

@Composable
fun appAnimateDpAsState(
    targetValue: Dp,
    animationSpec: AnimationSpec<Dp> = dpDefaultSpring,
    label: String = "DpAnimation",
    finishedListener: ((Dp) -> Unit)? = null,
): State<Dp> = if (shouldAnimate()) animateDpAsState(
    targetValue,
    animationSpec,
    label,
    finishedListener
) else derivedStateOf { targetValue }

@Composable
fun appAnimateColorAsState(
    targetValue: Color,
    animationSpec: AnimationSpec<Color> = colorDefaultSpring,
    label: String = "ColorAnimation",
    finishedListener: ((Color) -> Unit)? = null,
): State<Color> = if (shouldAnimate()) animateColorAsState(
    targetValue,
    animationSpec,
    label,
    finishedListener
) else derivedStateOf { targetValue }

@Composable
fun appAnimateFloatAsState(
    targetValue: Float,
    animationSpec: AnimationSpec<Float> = defaultAnimation,
    visibilityThreshold: Float = 0.01f,
    label: String = "FloatAnimation",
    finishedListener: ((Float) -> Unit)? = null,
): State<Float> = if (shouldAnimate()) animateFloatAsState(
    targetValue,
    animationSpec,
    visibilityThreshold,
    label,
    finishedListener
) else derivedStateOf { targetValue }

@Composable
fun Modifier.appAnimateContentSize(
    animationSpec: FiniteAnimationSpec<IntSize> =
        spring(
            stiffness = StiffnessMediumLow,
            visibilityThreshold = IntSize.VisibilityThreshold,
        ),
    finishedListener: ((initialValue: IntSize, targetValue: IntSize) -> Unit)? = null,
): Modifier = if (shouldAnimate()) animateContentSize(animationSpec, finishedListener) else this

@Composable
fun InfiniteTransition.appAnimateColor(
    initialValue: Color,
    targetValue: Color,
    animationSpec: InfiniteRepeatableSpec<Color>,
    label: String = "ColorAnimation",
): State<Color> = if (shouldAnimate()) animateColor(
    initialValue,
    targetValue,
    animationSpec,
    label
) else derivedStateOf { targetValue }

@Composable
fun InfiniteTransition.appAnimateFloat(
    initialValue: Float,
    targetValue: Float,
    animationSpec: InfiniteRepeatableSpec<Float>,
    label: String = "FloatAnimation",
): State<Float> = if (shouldAnimate()) animateFloat(
    initialValue,
    targetValue,
    animationSpec,
    label
) else derivedStateOf { targetValue }

@Composable
fun AppAnimatedVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    enter: EnterTransition = fadeIn() + expandIn(),
    exit: ExitTransition = shrinkOut() + fadeOut(),
    label: String = "AnimatedVisibility",
    content: @Composable() () -> Unit,
) {
    if (shouldAnimate()) AnimatedVisibility(visible, modifier, enter, exit, label) { content() }
    else if (visible) content()
}

@Composable
fun RowScope.AppAnimatedVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    enter: EnterTransition = fadeIn() + expandHorizontally(),
    exit: ExitTransition = fadeOut() + shrinkHorizontally(),
    label: String = "AnimatedVisibility",
    content: @Composable () -> Unit,
) {
    if (shouldAnimate()) AnimatedVisibility(visible, modifier, enter, exit, label) { content() }
    else if (visible) content()
}

@Composable
fun ColumnScope.AppAnimatedVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    enter: EnterTransition = fadeIn() + expandHorizontally(),
    exit: ExitTransition = fadeOut() + shrinkHorizontally(),
    label: String = "AnimatedVisibility",
    content: @Composable () -> Unit,
) {
    if (shouldAnimate()) AnimatedVisibility(visible, modifier, enter, exit, label) { content() }
    else if (visible) content()
}

@Composable
fun <S> AppAnimatedContent(
    targetState: S,
    modifier: Modifier = Modifier,
    transitionSpec: AnimatedContentTransitionScope<S>.() -> ContentTransform = {
        (fadeIn(animationSpec = tween(220, delayMillis = 90)) +
                scaleIn(initialScale = 0.92f, animationSpec = tween(220, delayMillis = 90)))
            .togetherWith(fadeOut(animationSpec = tween(90)))
    },
    contentAlignment: Alignment = Alignment.TopStart,
    label: String = "AnimatedContent",
    contentKey: (targetState: S) -> Any? = { it },
    content: @Composable() (targetState: S) -> Unit,
) {
    if (shouldAnimate()) AnimatedContent(
        targetState,
        modifier,
        transitionSpec,
        contentAlignment,
        label,
        contentKey
    ) { content(targetState) }
    else content(targetState)
}

@Composable
context(scope: SharedTransitionScope)
fun Modifier.appSharedElement(
    sharedContentState: SharedContentState,
    animatedVisibilityScope: AnimatedVisibilityScope,
    boundsTransform: BoundsTransform = DefaultBoundsTransform,
    placeholderSize: PlaceholderSize = ContentSize,
    renderInOverlayDuringTransition: Boolean = true,
    zIndexInOverlay: Float = 0f,
    clipInOverlayDuringTransition: OverlayClip = ParentClip,
): Modifier = if (shouldAnimate()) with(scope) {
    sharedElement(
        sharedContentState,
        animatedVisibilityScope,
        boundsTransform,
        placeholderSize,
        renderInOverlayDuringTransition,
        zIndexInOverlay,
        clipInOverlayDuringTransition
    )
} else
    this

@Composable
context(scope: SharedTransitionScope)
fun Modifier.appSharedBounds(
    sharedContentState: SharedContentState,
    animatedVisibilityScope: AnimatedVisibilityScope,
    enter: EnterTransition = fadeIn(),
    exit: ExitTransition = fadeOut(),
    boundsTransform: BoundsTransform = SharedTransitionDefaults.BoundsTransform,
    resizeMode: ResizeMode = scaleToBounds(ContentScale.FillWidth, Center),
    placeholderSize: PlaceholderSize = ContentSize,
    renderInOverlayDuringTransition: Boolean = true,
    zIndexInOverlay: Float = 0f,
    clipInOverlayDuringTransition: OverlayClip = ParentClip,
): Modifier = if (shouldAnimate()) with(scope) {
    sharedBounds(
        sharedContentState,
        animatedVisibilityScope,
        enter,
        exit,
        boundsTransform,
        resizeMode,
        placeholderSize,
        renderInOverlayDuringTransition,
        zIndexInOverlay,
        clipInOverlayDuringTransition
    )
} else this

@Composable
context(scope: LazyGridItemScope)
fun Modifier.appAnimateItem(
    fadeInSpec: FiniteAnimationSpec<Float>? = spring(stiffness = StiffnessMediumLow),
    placementSpec: FiniteAnimationSpec<IntOffset>? =
        spring(
            stiffness = StiffnessMediumLow,
            visibilityThreshold = IntOffset.VisibilityThreshold,
        ),
    fadeOutSpec: FiniteAnimationSpec<Float>? = spring(stiffness = StiffnessMediumLow),
): Modifier = if (shouldAnimate()) with(scope) { animateItem(fadeInSpec, placementSpec, fadeOutSpec) } else this

@Composable
context(scope: LazyItemScope)
fun Modifier.appAnimateItem(
    fadeInSpec: FiniteAnimationSpec<Float>? = spring(stiffness = StiffnessMediumLow),
    placementSpec: FiniteAnimationSpec<IntOffset>? =
        spring(
            stiffness = StiffnessMediumLow,
            visibilityThreshold = IntOffset.VisibilityThreshold,
        ),
    fadeOutSpec: FiniteAnimationSpec<Float>? = spring(stiffness = StiffnessMediumLow),
): Modifier = if (shouldAnimate()) with(scope) { animateItem(fadeInSpec, placementSpec, fadeOutSpec) } else this

@Composable
fun shouldAnimate(): Boolean = LocalAnimation.current

private val ParentClip: OverlayClip =
    object : OverlayClip {
        override fun getClipPath(
            sharedContentState: SharedContentState,
            bounds: Rect,
            layoutDirection: LayoutDirection,
            density: Density,
        ): Path? {
            return sharedContentState.parentSharedContentState?.clipPathInOverlay
        }
    }

private val DefaultBoundsTransform = BoundsTransform { _, _ -> DefaultSpring }

private val DefaultSpring =
    spring(stiffness = StiffnessMediumLow, visibilityThreshold = Rect.VisibilityThreshold)

private val defaultAnimation = spring<Float>()

private val dpDefaultSpring = spring(visibilityThreshold = Dp.VisibilityThreshold)

private val colorDefaultSpring = spring<Color>()
