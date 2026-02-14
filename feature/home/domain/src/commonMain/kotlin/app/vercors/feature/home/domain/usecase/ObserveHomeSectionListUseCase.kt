package app.vercors.feature.home.domain.usecase

import app.vercors.core.domain.filterIsRight
import app.vercors.feature.config.domain.model.HomeCategoryConfig
import app.vercors.feature.config.domain.usecase.ObserveConfigurationUseCase
import app.vercors.feature.home.domain.model.HomeConfigurationError
import app.vercors.feature.home.domain.model.HomeSectionError
import app.vercors.feature.home.domain.model.HomeSectionType
import app.vercors.feature.instance.domain.model.InstanceAndStatus
import app.vercors.feature.instance.domain.usecase.ObserveInstanceListUseCase
import app.vercors.feature.project.domain.model.*
import app.vercors.feature.project.domain.usecase.GetFeaturedProjectListUseCase
import arrow.core.raise.Raise
import arrow.core.raise.context.bind
import arrow.core.raise.withError
import dev.zacsweers.metro.Inject
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.*
import opensavvy.state.ExperimentalProgressiveRaiseApi
import opensavvy.state.arrow.progressive
import opensavvy.state.arrow.toEither
import opensavvy.state.coroutines.filterNotLoading
import opensavvy.state.outcome.map
import opensavvy.state.outcome.mapFailure
import opensavvy.state.progressive.ProgressiveOutcome
import opensavvy.state.progressive.map

private val logger = KotlinLogging.logger {}

@Inject
class ObserveHomeSectionListUseCase(
    private val observeConfiguration: ObserveConfigurationUseCase,
    private val observeInstanceList: ObserveInstanceListUseCase,
    private val getFeaturedProjectList: GetFeaturedProjectListUseCase,
) {
    @OptIn(ExperimentalProgressiveRaiseApi::class)
    context(_: Raise<HomeConfigurationError>)
    operator fun invoke(): Flow<List<HomeSection<*>>> {
        logger.debug { "Observing home section list" }
        val configFlow = observeConfiguration()
            .filterNotLoading()
            .map { outcome ->
                outcome
                    .map { it.preferredPlatform to it.homeCategories }
                    .mapFailure { HomeConfigurationError(it) }
                    .toEither().bind()
            }
            .distinctUntilChanged()
        val projectListFlow = configFlow
            .transform { (platformId, categories) -> loadFeaturedProjectList(platformId, categories) }
            .distinctUntilChanged()
        val instanceFlow = observeInstanceList()
            .map { res -> res.map { list -> list.filterIsRight().sortedBy { it.instance.lastPlayedAt }.take(10) } }
            .distinctUntilChanged()

        return combine(configFlow, projectListFlow, instanceFlow) { (_, categories), projectList, instances ->
            categories.map { it.toSection(instances, projectList) }
        }
    }

    @OptIn(ExperimentalProgressiveRaiseApi::class)
    private suspend fun FlowCollector<ProgressiveOutcome<HomeSectionError, FeaturedProjectList>>.loadFeaturedProjectList(
        platformId: PlatformId,
        categories: List<HomeCategoryConfig>
    ) {
        emit(ProgressiveOutcome.Incomplete())
        if (categories.any { it.type == HomeCategoryConfig.Type.Project }) {
            val outcome = progressive {
                withError<HomeSectionError, PlatformError, FeaturedProjectList>(
                    { HomeSectionError.PlatformError(it.error, it.description) }
                ) {
                    withError<HomeSectionError, PlatformNotFound, FeaturedProjectList>(
                        { HomeSectionError.PlatformNotFound(platformId) }
                    ) {
                        getFeaturedProjectList(platformId)
                    }
                }
            }
            emit(outcome)
        }
    }

    private fun HomeCategoryConfig.toSection(
        instances: ProgressiveOutcome<Nothing, List<InstanceAndStatus>>,
        featuredProjectList: ProgressiveOutcome<HomeSectionError, FeaturedProjectList>
    ): HomeSection<*> = when (this) {
        HomeCategoryConfig.Instances -> HomeSection.InstanceListSection(
            type = HomeSectionType.RecentlyPlayed,
            data = instances
        )

        HomeCategoryConfig.Modpacks -> HomeSection.ProjectListSection(
            type = HomeSectionType.FeaturedModpacks,
            data = featuredProjectList.map { it.getValue(ProjectType.Modpack) }
        )

        HomeCategoryConfig.Mods -> HomeSection.ProjectListSection(
            type = HomeSectionType.FeaturedMods,
            data = featuredProjectList.map { it.getValue(ProjectType.Mod) }
        )

        HomeCategoryConfig.ResourcePacks -> HomeSection.ProjectListSection(
            type = HomeSectionType.FeaturedResourcePacks,
            data = featuredProjectList.map { it.getValue(ProjectType.ResourcePack) }
        )

        HomeCategoryConfig.ShaderPacks -> HomeSection.ProjectListSection(
            type = HomeSectionType.FeaturedShaderPacks,
            data = featuredProjectList.map { it.getValue(ProjectType.ShaderPack) }
        )
    }
}

sealed interface HomeSection<T : Any> {
    val type: HomeSectionType
    val data: ProgressiveOutcome<HomeSectionError, List<T>>

    data class InstanceListSection(
        override val type: HomeSectionType,
        override val data: ProgressiveOutcome<HomeSectionError, List<InstanceAndStatus>>
    ) : HomeSection<InstanceAndStatus>

    data class ProjectListSection(
        override val type: HomeSectionType,
        override val data: ProgressiveOutcome<HomeSectionError, List<BriefProject>>
    ) : HomeSection<BriefProject>
}
