package app.vercors.launcher.gui

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import app.vercors.core.domain.AppConstants
import app.vercors.core.network.MetaApiConnectivityPoller
import app.vercors.feature.instance.domain.usecase.CreateInstanceUseCase
import app.vercors.feature.instance.domain.usecase.DeleteInstanceUseCase
import app.vercors.feature.instance.domain.usecase.ObserveInstanceListUseCase
import app.vercors.shared.data.AppPaths
import app.vercors.shared.ui.SnackbarService
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.animation.AnimatedNavDecorator
import com.slack.circuit.runtime.navigation.NavArgument
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.ui.Ui
import dev.nucleusframework.core.runtime.NucleusApp
import dev.zacsweers.metro.*
import io.ktor.client.*
import kotlinx.coroutines.CoroutineScope

@DependencyGraph(AppScope::class)
internal interface AppGraph {
    val appPaths: AppPaths
    val circuit: Circuit
    val snackbarService: SnackbarService
    val metaApiConnectivityPoller: MetaApiConnectivityPoller
    val httpClient: HttpClient
    val observeInstanceList: ObserveInstanceListUseCase
    val createInstance: CreateInstanceUseCase
    val deleteInstance: DeleteInstanceUseCase

    @Named(AppConstants.APP_NAME_KEY)
    val appName: String

    @Provides
    @SingleIn(AppScope::class)
    @Named(AppConstants.APP_ID_KEY)
    fun provideAppId(): String = NucleusApp.appId

    @Provides
    @SingleIn(AppScope::class)
    @Named(AppConstants.APP_NAME_KEY)
    fun provideAppName(): String = NucleusApp.appName ?: "unknown"

    @Provides
    @SingleIn(AppScope::class)
    @Named(AppConstants.APP_VERSION_KEY)
    fun provideAppVersion(): String = NucleusApp.version ?: "unknown"

    @Provides
    @SingleIn(AppScope::class)
    @Named(AppConstants.APP_VENDOR_KEY)
    fun provideAppVendor(): String = NucleusApp.vendor ?: "unknown"

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

internal val LocalAppGraph: ProvidableCompositionLocal<AppGraph> =
    staticCompositionLocalOf {
        error("LocalAppGraph not provided — use it inside CircuitContent(AppScreen).")
    }
