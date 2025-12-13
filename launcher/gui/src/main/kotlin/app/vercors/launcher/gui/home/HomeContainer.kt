package app.vercors.launcher.gui.home

import app.vercors.launcher.common.domain.instance.model.Instance
import app.vercors.launcher.common.domain.instance.model.InstanceId
import app.vercors.launcher.gui.home.usecase.HomeSection
import app.vercors.launcher.gui.home.usecase.ObserveHomeSectionListUseCase
import app.vercors.shared.domain.Resource
import app.vercors.shared.domain.Result
import app.vercors.shared.domain.project.model.BriefProject
import app.vercors.shared.domain.project.model.ProjectId
import app.vercors.shared.ui.configureWithLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.logging.logt
import pro.respawn.flowmvi.plugins.reduce
import pro.respawn.flowmvi.plugins.whileSubscribed
import kotlin.time.Instant

private val logger = KotlinLogging.logger {}

class HomeContainer(
    val observeHomeSectionList: ObserveHomeSectionListUseCase,
) : Container<HomeState, HomeIntent, HomeAction> {
    override val store = store(initial = HomeState.Loading) {
        configureWithLogger(logger)

        reduce { intent ->
            logt { "Intent: $intent" }
            when (intent) {
                HomeIntent.OnCreateInstanceClick -> action(HomeAction.NavigateToCreateInstance)
                is HomeIntent.OnInstanceClick -> action(HomeAction.NavigateToInstanceDetails(intent.instanceId))
                is HomeIntent.OnProjectClick -> action(HomeAction.NavigateToProjectDetails(intent.projectId))
            }
        }

        whileSubscribed {
            observeHomeSectionList().collect {
                updateState {
                    logt { "HomeSectionList: $it" }
                    when (it) {
                        is Resource.Loaded -> when (val res = it.result) {
                            is Result.Success -> HomeState.Loaded(res.data.map { section ->
                                when (section) {
                                    is HomeSection.InstanceListSection -> HomeSectionView.InstanceListSection(
                                        section.type,
                                        section.data.map { it.toBriefView() })

                                    is HomeSection.ProjectListSection -> HomeSectionView.ProjectListSection(
                                        section.type,
                                        section.data.map { it.toView() })
                                }
                            })

                            is Result.Error -> HomeState.Error(res.throwable)
                        }

                        else -> HomeState.Loading
                    }
                }
            }
        }
    }
}

sealed interface HomeState : MVIState {
    data object Loading : HomeState
    data class Loaded(val sections: List<HomeSectionView<*>>) : HomeState
    data class Error(val throwable: Throwable) : HomeState
}

sealed interface HomeSectionView<T : HomeSectionViewElement> {
    val type: HomeSectionType
    val data: List<T>

    data class InstanceListSection(
        override val type: HomeSectionType,
        override val data: List<BriefInstanceView>
    ) : HomeSectionView<BriefInstanceView>

    data class ProjectListSection(
        override val type: HomeSectionType,
        override val data: List<BriefProjectView>
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
}

enum class HomeSectionType {
    RecentlyPlayed,
    FeaturedMods,
    FeaturedModpacks,
    FeaturedResourcePacks,
    FeaturedShaderPacks
}

sealed interface HomeSectionViewElement {
    val id: Any
}

data class BriefInstanceView(
    override val id: String,
    val name: String,
    val version: String
) : HomeSectionViewElement

data class BriefProjectView(
    override val id: ProjectId,
    val name: String,
    val description: String,
    val author: String,
    val url: String,
    val downloads: Long,
    val lastUpdated: Instant,
    val logoUrl: String?,
    val imageUrl: String?
) : HomeSectionViewElement

private fun Instance.toBriefView(): BriefInstanceView = BriefInstanceView(
    id = id,
    name = name,
    version = gameVersion
)

private fun BriefProject.toView(): BriefProjectView = BriefProjectView(
    id = id,
    name = name,
    description = description,
    author = author,
    url = url,
    downloads = downloads,
    lastUpdated = lastUpdated,
    logoUrl = logoUrl,
    imageUrl = imageUrl
)
