package app.vercors.launcher.home.data

import app.vercors.launcher.core.config.model.HomeProviderConfig
import app.vercors.launcher.core.config.repository.ConfigRepository
import app.vercors.launcher.home.domain.HomeRepository
import app.vercors.launcher.home.domain.HomeSection
import app.vercors.launcher.home.domain.HomeSection.Instances
import app.vercors.launcher.home.domain.HomeSection.Projects
import app.vercors.launcher.home.domain.HomeSectionData
import app.vercors.launcher.home.domain.HomeSectionData.Loaded
import app.vercors.launcher.home.domain.HomeSectionType
import app.vercors.launcher.instance.domain.Instance
import app.vercors.launcher.instance.domain.InstanceRepository
import app.vercors.launcher.project.domain.ProjectRepository
import app.vercors.launcher.project.domain.ProjectType
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single

private val logger = KotlinLogging.logger {}

@Single
class HomeRepositoryImpl(
    private val configRepository: ConfigRepository,
    private val instanceRepository: InstanceRepository,
    private val projectRepository: ProjectRepository
) : HomeRepository {
    private val sectionCache: MutableMap<HomeSectionCacheKey, HomeSection> = mutableMapOf()

    private val _sectionsState = MutableStateFlow<List<HomeSection>>(emptyList())
    override val sectionsState = _sectionsState.asStateFlow()

    override suspend fun loadSections() = coroutineScope {
        combine(
            configRepository.observeConfig().map { it.home },
            instanceRepository.observeAll()
        ) { config, instance -> config to instance }
            .distinctUntilChanged()
            .collect { (config, instances) ->
                val (sectionConfigs, providerConfig) = config
                logger.debug { "Processing home config changes: $sectionConfigs, $providerConfig" }
                val sectionTypes = sectionConfigs.map { it.toType() }
                _sectionsState.update { sections -> sections.filter { it.type in sectionTypes } }
                sectionTypes.map { sectionType -> launch { loadSection(providerConfig, instances, sectionType) } }
                    .joinAll()
                logger.debug { "Finished processing home config changes" }
            }
    }

    private suspend fun loadSection(
        providerConfig: HomeProviderConfig,
        instances: List<Instance>,
        sectionType: HomeSectionType
    ) {
        when (sectionType) {
            HomeSectionType.JumpBackIn -> updateSection(
                instances
                    .sortedByDescending { it.lastPlayedAt }
                    .let { Instances(sectionType, Loaded(it)) })

            else -> {
                val providerType = providerConfig.toType()
                val cacheKey = HomeSectionCacheKey(sectionType, providerConfig)
                val section = sectionCache.getOrPut(cacheKey) {
                    logger.debug { "Home section cache miss for key $cacheKey" }
                    updateSection(createLoadingSection(sectionType))
                    return@getOrPut when (sectionType) {
                        HomeSectionType.PopularMods -> projectRepository.findProjects(
                            providerType,
                            ProjectType.Mod
                        ).first()
                            .let { Projects(sectionType, Loaded(it)) }

                        HomeSectionType.PopularModpacks -> projectRepository.findProjects(
                            providerType,
                            ProjectType.Modpack
                        ).first()
                            .let { Projects(sectionType, Loaded(it)) }

                        HomeSectionType.PopularResourcePacks -> projectRepository.findProjects(
                            providerType,
                            ProjectType.ResourcePack
                        ).first()
                            .let { Projects(sectionType, Loaded(it)) }

                        HomeSectionType.PopularShaderPacks -> projectRepository.findProjects(
                            providerType,
                            ProjectType.ShaderPack
                        ).first()
                            .let { Projects(sectionType, Loaded(it)) }

                        HomeSectionType.JumpBackIn -> throw IllegalStateException("Unreachable")
                    }
                }
                updateSection(section)
            }
        }
    }

    private fun createLoadingSection(type: HomeSectionType): HomeSection = when (type) {
        HomeSectionType.JumpBackIn -> Instances(HomeSectionType.JumpBackIn, HomeSectionData.Loading())
        else -> Projects(type, HomeSectionData.Loading())
    }

    private fun updateSection(section: HomeSection) {
        logger.debug { "Updating section: ${section.type}" }
        logger.trace { "Section data: $section" }
        _sectionsState.update { sections ->
            if (sections.any { it.type == section.type }) sections.map { if (it.type == section.type) section else it }
            else (sections + section).sortedBy { it.type }
        }
    }

    private data class HomeSectionCacheKey(val type: HomeSectionType, val provider: HomeProviderConfig)
}