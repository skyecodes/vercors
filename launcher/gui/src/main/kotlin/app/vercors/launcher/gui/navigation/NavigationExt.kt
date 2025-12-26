package app.vercors.launcher.gui.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import app.vercors.shared.ui.core.animation.LocalSharedTransitionScope
import org.koin.compose.navigation3.EntryProviderInstaller
import org.koin.core.definition.KoinDefinition
import org.koin.core.module.Module
import org.koin.core.scope.Scope
import org.koin.dsl.navigation3.navigation

inline fun <reified T : Any> Module.appNavigation(
    metadata: Map<String, Any> = emptyMap(),
    noinline definition: @Composable context(AnimatedContentScope, SharedTransitionScope) Scope.(T, Navigator) -> Unit,
): KoinDefinition<EntryProviderInstaller> = navigation<T>(metadata) {
    with(LocalNavAnimatedContentScope.current) {
        with(LocalSharedTransitionScope.current) {
            definition(it, LocalNavigator.current)
        }
    }
}
