package app.vercors.feature.home.ui

import androidx.compose.runtime.Immutable
import app.vercors.core.domain.Resource
import app.vercors.core.domain.Result
import app.vercors.feature.home.domain.model.HomeSectionType
import app.vercors.feature.home.domain.usecase.HomeSection
import app.vercors.feature.home.domain.usecase.ObserveHomeSectionListUseCase
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.ui.BriefInstanceView
import app.vercors.feature.instance.ui.toBriefView
import app.vercors.feature.project.domain.model.ProjectId
import app.vercors.feature.project.ui.BriefProjectView
import app.vercors.feature.project.ui.toView
import app.vercors.shared.ui.configureWithLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.reduce
import pro.respawn.flowmvi.plugins.whileSubscribed

private val logger = KotlinLogging.logger {}

class HomeContainer(
    val observeHomeSectionList: ObserveHomeSectionListUseCase,
) : Container<HomeState, HomeIntent, HomeAction> {
    override val store = store(initial = HomeState.Loading) {
        configureWithLogger(logger)

        reduce { intent ->
            when (intent) {
                HomeIntent.OnCreateInstanceClick -> action(HomeAction.NavigateToCreateInstance)
                is HomeIntent.OnInstanceClick -> action(HomeAction.NavigateToInstanceDetails(intent.instanceId))
                is HomeIntent.OnProjectClick -> action(HomeAction.NavigateToProjectDetails(intent.projectId))
            }
        }

        whileSubscribed {
            observeHomeSectionList().collect {
                when (it) {
                    is Resource.Loaded -> when (val res = it.result) {
                        is Result.Success -> updateState {
                            HomeState.Loaded(res.data.map { section ->
                                when (section) {
                                    is HomeSection.InstanceListSection -> HomeSectionView.InstanceListSection(
                                        section.type,
                                        section.data?.map { it.toBriefView() })

                                    is HomeSection.ProjectListSection -> HomeSectionView.ProjectListSection(
                                        section.type,
                                        section.data?.map { it.toView() })
                                }
                            })
                        }

                        is Result.Failure -> action(HomeAction.ShowErrorPopup(res.error))
                    }

                    else -> updateState { HomeState.Loading }
                }
            }
        }
    }
}

@Immutable
sealed interface HomeState : MVIState {
    data object Loading : HomeState

    @Immutable
    data class Loaded(val sections: List<HomeSectionView<*>>) : HomeState

    @Immutable
    data class Error(val throwable: Throwable) : HomeState
}

sealed interface HomeSectionView<T> {
    val type: HomeSectionType
    val data: List<T>?

    data class InstanceListSection(
        override val type: HomeSectionType,
        override val data: List<BriefInstanceView>?
    ) : HomeSectionView<BriefInstanceView>

    data class ProjectListSection(
        override val type: HomeSectionType,
        override val data: List<BriefProjectView>?
    ) : HomeSectionView<BriefProjectView>
}

sealed interface HomeIntent : MVIIntent {
    data object OnCreateInstanceClick : HomeIntent
    data class OnProjectClick(val projectId: ProjectId) : HomeIntent
    data class OnInstanceClick(val instanceId: InstanceId) : HomeIntent
}

sealed interface HomeAction : MVIAction {
    data object NavigateToCreateInstance : HomeAction
    data class NavigateToProjectDetails(val projectId: ProjectId) : HomeAction
    data class NavigateToInstanceDetails(val instanceId: InstanceId) : HomeAction
    data class ShowErrorPopup(val throwable: Throwable) : HomeAction
}
