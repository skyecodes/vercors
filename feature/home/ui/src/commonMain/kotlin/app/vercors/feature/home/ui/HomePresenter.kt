package app.vercors.feature.home.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.vercors.core.domain.orNull
import app.vercors.feature.home.domain.usecase.HomeSection
import app.vercors.feature.home.domain.usecase.ObserveHomeSectionListUseCase
import app.vercors.feature.instance.ui.create.CreateInstanceScreen
import app.vercors.feature.instance.ui.details.InstanceDetailsScreen
import app.vercors.feature.instance.ui.toBriefView
import app.vercors.feature.project.ui.details.ProjectDetailsScreen
import app.vercors.feature.project.ui.toView
import app.vercors.shared.ui.Presenter
import app.vercors.shared.ui.Store
import app.vercors.shared.ui.plus
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.mapNotNull

private val logger = KotlinLogging.logger {}

@AssistedInject
class HomePresenter(
    @Assisted val navigator: Navigator,
    val observeHomeSectionList: ObserveHomeSectionListUseCase
) : Presenter<HomeState, HomeEvent> {
    @Composable
    override fun present(): Store<HomeState, HomeEvent> {
        val sections by remember(observeHomeSectionList) {
            observeHomeSectionList().mapNotNull { res ->
                res.orNull()?.map {
                    when (it) {
                        is HomeSection.InstanceListSection -> HomeState.Section.InstanceList(
                            type = it.type,
                            data = it.data?.map { it.toBriefView() }
                        )

                        is HomeSection.ProjectListSection -> HomeState.Section.ProjectList(
                            type = it.type,
                            data = it.data?.map { it.toView() }
                        )
                    }
                }
            }
        }.collectAsStateWithLifecycle(emptyList())

        return HomeState(
            sections = sections
        ) + {
            when (it) {
                is HomeEvent.OnCreateInstanceClick -> navigator.goTo(CreateInstanceScreen)
                is HomeEvent.OnProjectClick -> navigator.goTo(ProjectDetailsScreen(it.projectId))
                is HomeEvent.OnInstanceClick -> navigator.goTo(InstanceDetailsScreen(it.instanceId))
            }
        }
    }

    @CircuitInject(HomeScreen::class, AppScope::class)
    @AssistedFactory
    fun interface Factory {
        fun create(navigator: Navigator): HomePresenter
    }
}
