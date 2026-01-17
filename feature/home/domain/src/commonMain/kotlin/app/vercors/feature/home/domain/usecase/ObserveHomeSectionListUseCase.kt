package app.vercors.feature.home.domain.usecase

import app.vercors.core.domain.Resource
import app.vercors.core.domain.combineResource
import app.vercors.core.domain.map
import app.vercors.core.domain.orThrow
import app.vercors.feature.config.gui.domain.model.HomeCategoryConfig
import app.vercors.feature.config.gui.domain.usecase.ObserveGuiConfigurationUseCase
import app.vercors.feature.home.domain.model.HomeSectionType
import app.vercors.feature.instance.domain.model.InstanceAndStatus
import app.vercors.feature.instance.domain.usecase.ObserveInstanceListUseCase
import app.vercors.feature.project.domain.model.BriefProject
import app.vercors.feature.project.domain.model.PlatformId
import app.vercors.feature.project.domain.model.ProjectType
import app.vercors.feature.project.domain.usecase.GetFeaturedProjectListUseCase
import dev.zacsweers.metro.Inject
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transform

private val logger = KotlinLogging.logger {}

@Inject
class ObserveHomeSectionListUseCase(
    private val observeGuiConfiguration: ObserveGuiConfigurationUseCase,
    private val observeInstanceList: ObserveInstanceListUseCase,
    private val getFeaturedProjectList: GetFeaturedProjectListUseCase,
) {
    operator fun invoke(): Flow<Resource<List<HomeSection<*>>>> {
        logger.debug { "Observing home section list" }
        val configFlow = observeGuiConfiguration()
            .map { res -> res.map { it.preferredPlatform to it.homeCategories } }
            .distinctUntilChanged()
        val instanceFlow = observeInstanceList()
            .map { res -> res.map { list -> list.sortedBy { it.instance.lastPlayedAt }.take(10) } }
            .distinctUntilChanged()
        return configFlow.combineResource(instanceFlow).transform { resources ->
            logger.debug { "Resources: $resources" }
            emit(resources.map { (config, instances) ->
                val (_, categories) = config
                categories.map { it.toSection(instances, null) }
            })
            emit(resources.map { (config, instances) ->
                val (platformId, categories) = config
                val featuredProjectList = loadFeaturedProjectList(platformId, categories)
                categories.map { it.toSection(instances, featuredProjectList) }
            })
        }
    }

    private suspend fun loadFeaturedProjectList(
        platformId: PlatformId,
        categories: List<HomeCategoryConfig>
    ): Map<ProjectType, List<BriefProject>> =
        if (categories.any { it.type == HomeCategoryConfig.Type.Project })
            getFeaturedProjectList(platformId).orThrow()
        else emptyMap()

    private fun HomeCategoryConfig.toSection(
        instances: List<InstanceAndStatus>,
        featuredProjectList: Map<ProjectType, List<BriefProject>>?
    ): HomeSection<*> = when (this) {
        HomeCategoryConfig.Instances -> HomeSection.InstanceListSection(
            type = HomeSectionType.RecentlyPlayed,
            data = instances
        )

        HomeCategoryConfig.Modpacks -> HomeSection.ProjectListSection(
            type = HomeSectionType.FeaturedModpacks,
            data = featuredProjectList?.getValue(ProjectType.Modpack)
        )

        HomeCategoryConfig.Mods -> HomeSection.ProjectListSection(
            type = HomeSectionType.FeaturedMods,
            data = featuredProjectList?.getValue(ProjectType.Mod)
        )

        HomeCategoryConfig.ResourcePacks -> HomeSection.ProjectListSection(
            type = HomeSectionType.FeaturedResourcePacks,
            data = featuredProjectList?.getValue(ProjectType.ResourcePack)
        )

        HomeCategoryConfig.ShaderPacks -> HomeSection.ProjectListSection(
            type = HomeSectionType.FeaturedShaderPacks,
            data = featuredProjectList?.getValue(ProjectType.ShaderPack)
        )
    }
}

sealed interface HomeSection<T : Any> {
    val type: HomeSectionType
    val data: List<T>?

    data class InstanceListSection(
        override val type: HomeSectionType,
        override val data: List<InstanceAndStatus>?
    ) : HomeSection<InstanceAndStatus>

    data class ProjectListSection(
        override val type: HomeSectionType,
        override val data: List<BriefProject>?
    ) : HomeSection<BriefProject>
}
