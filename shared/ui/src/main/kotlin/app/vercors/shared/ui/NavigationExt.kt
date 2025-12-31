package app.vercors.shared.ui

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import app.vercors.core.ui.animation.LocalSharedTransitionScope
import org.koin.compose.navigation3.EntryProviderInstaller
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.definition.KoinDefinition
import org.koin.core.module.Module
import org.koin.core.scope.Scope
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
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
