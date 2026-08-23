package app.vercors.launcher.gui

import app.vercors.core.network.MetaApiConnectivityPoller
import app.vercors.shared.data.AppPaths
import app.vercors.shared.ui.SnackbarService
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.animation.AnimatedNavDecorator
import com.slack.circuit.runtime.navigation.NavArgument
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.ui.Ui
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope

@DependencyGraph(AppScope::class)
internal interface AppGraph {
    val appPaths: AppPaths
    val circuit: Circuit
    val snackbarService: SnackbarService
    val metaApiConnectivityPoller: MetaApiConnectivityPoller

    @Provides
    @SingleIn(AppScope::class)
    fun provideCircuit(
        presenterFactories: Set<Presenter.Factory>,
        uiFactories: Set<Ui.Factory>,
    ): Circuit {
        return Circuit.Builder()
            .addPresenterFactories(presenterFactories)
            .addUiFactories(uiFactories)
            .setAnimatedNavDecoratorFactory(object : AnimatedNavDecorator.Factory {
                override fun <T : NavArgument> create(): AnimatedNavDecorator<T, *> {
                    return AppDecorator()
                }
            })
            .build()
    }

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides coroutineScope: CoroutineScope): AppGraph
    }
}
