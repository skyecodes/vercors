package app.vercors.launcher.gui

import androidx.compose.animation.*
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.updateTransition
import androidx.compose.runtime.Composable
import com.slack.circuit.foundation.NavigatorDefaults
import com.slack.circuit.foundation.animation.AnimatedNavDecorator
import com.slack.circuit.foundation.animation.AnimatedNavEvent
import com.slack.circuit.foundation.animation.AnimatedNavState
import com.slack.circuit.runtime.InternalCircuitApi
import com.slack.circuit.runtime.navigation.NavArgument
import com.slack.circuit.runtime.navigation.NavStackList

class AppDecorator<T : NavArgument> :
    AnimatedNavDecorator<T, NavigatorDefaults.DefaultDecorator.DefaultAnimatedState<T>> {

    override fun targetState(args: NavStackList<T>): NavigatorDefaults.DefaultDecorator.DefaultAnimatedState<T> {
        return NavigatorDefaults.DefaultDecorator.DefaultAnimatedState(args)
    }

    @Composable
    override fun updateTransition(args: NavStackList<T>): Transition<NavigatorDefaults.DefaultDecorator.DefaultAnimatedState<T>> {
        return updateTransition(targetState(args))
    }

    @OptIn(InternalCircuitApi::class)
    override fun AnimatedContentTransitionScope<AnimatedNavState>.transitionSpec(
        animatedNavEvent: AnimatedNavEvent
    ): ContentTransform {
        return (fadeIn() togetherWith fadeOut()).using(
            SizeTransform(clip = false)
        )
    }

    @Composable
    override fun AnimatedContentScope.Decoration(
        targetState: NavigatorDefaults.DefaultDecorator.DefaultAnimatedState<T>,
        innerContent: @Composable (T) -> Unit,
    ) {
        innerContent(targetState.navStack.first())
    }
}
