package app.vercors.feature.home.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.vercors.feature.home.domain.usecase.HomeSection
import app.vercors.feature.home.domain.usecase.ObserveHomeSectionListUseCase
import app.vercors.feature.instance.ui.create.CreateInstanceScreen
import app.vercors.feature.instance.ui.details.InstanceDetailsScreen
import app.vercors.feature.instance.ui.toBriefView
import app.vercors.feature.project.ui.details.ProjectDetailsScreen
import app.vercors.feature.project.ui.toView
import app.vercors.shared.ui.Presenter
import app.vercors.shared.ui.Store
import arrow.core.raise.context.either
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.map
import opensavvy.state.ExperimentalProgressiveRaiseApi
import opensavvy.state.progressive.map

private val logger = KotlinLogging.logger {}

@AssistedInject
class HomePresenter(
    @Assisted val navigator: Navigator,
    val observeHomeSectionList: ObserveHomeSectionListUseCase
) : Presenter<HomeState, HomeEvent> {
    @OptIn(ExperimentalProgressiveRaiseApi::class)
    @Composable
    override fun present(): Store<HomeState, HomeEvent> {
        val sections by remember(observeHomeSectionList) {
            either { observeHomeSectionList() }.fold(
                ifLeft = { error("A configuration error occurred: $it") }, // TODO check if error handling can be better here
                ifRight = { flow ->
                    flow.map { list ->
                        list.map { section ->
                            when (section) {
                                is HomeSection.InstanceListSection -> HomeState.Section.InstanceList(
                                    type = section.type,
                                    data = section.data.map { list -> list.map { it.toBriefView() } }
                                )

                                is HomeSection.ProjectListSection -> HomeState.Section.ProjectList(
                                    type = section.type,
                                    data = section.data.map { list -> list.map { it.toView() } }
                                )
                            }
                        }
                    }
                }
            )
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
