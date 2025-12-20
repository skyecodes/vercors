package app.vercors.launcher.gui.home.usecase

import app.vercors.launcher.common.domain.instance.model.Instance
import app.vercors.launcher.common.domain.instance.usecase.ObserveInstanceListUseCase
import app.vercors.launcher.gui.config.model.HomeCategoryConfig
import app.vercors.launcher.gui.config.usecase.ObserveGuiConfigurationUseCase
import app.vercors.launcher.gui.home.HomeSectionType
import app.vercors.shared.domain.Resource
import app.vercors.shared.domain.combineResource
import app.vercors.shared.domain.getOrThrow
import app.vercors.shared.domain.map
import app.vercors.shared.domain.project.model.BriefProject
import app.vercors.shared.domain.project.model.ProjectPlatform
import app.vercors.shared.domain.project.model.ProjectType
import app.vercors.shared.domain.project.usecase.GetFeaturedProjectListUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transform
import org.koin.core.annotation.Factory

@Factory
class ObserveHomeSectionListUseCase(
    private val observeGuiConfiguration: ObserveGuiConfigurationUseCase,
    private val observeInstanceList: ObserveInstanceListUseCase,
    private val getFeaturedProjectList: GetFeaturedProjectListUseCase,
) {
    operator fun invoke(): Flow<Resource<List<HomeSection<*>>>> {
        val configFlow = observeGuiConfiguration().map { res -> res.map { it.preferredPlatform to it.homeCategories } }
            .distinctUntilChanged()
        val instanceFlow = observeInstanceList().map { res -> res.map { list -> list.sortedBy { it.lastPlayedAt } } }
            .distinctUntilChanged()
        return configFlow.combineResource(instanceFlow).transform { resources ->
            emit(resources.map { (config, instances) ->
                val (_, categories) = config
                categories.map { it.toSection(instances, null) }
            })
            emit(resources.map { (config, instances) ->
                val (platform, categories) = config
                val featuredProjectList = loadFeaturedProjectList(platform, categories)
                categories.map { it.toSection(instances, featuredProjectList) }
            })
        }
    }

    private suspend fun loadFeaturedProjectList(
        platform: ProjectPlatform,
        categories: List<HomeCategoryConfig>
    ): Map<ProjectType, List<BriefProject>> =
        if (categories.any { it.type == HomeCategoryConfig.Type.Project })
            getFeaturedProjectList(platform).getOrThrow()
        else emptyMap()

    private fun HomeCategoryConfig.toSection(
        instances: List<Instance>,
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
        override val data: List<Instance>?
    ) : HomeSection<Instance>

    data class ProjectListSection(
        override val type: HomeSectionType,
        override val data: List<BriefProject>?
    ) : HomeSection<BriefProject>
}
