package app.vercors.feature.instance.domain.usecase

import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.model.options.*
import app.vercors.feature.instance.domain.repository.InstanceOptionsRepository
import app.vercors.feature.instance.domain.repository.InstanceRepository
import arrow.core.raise.Raise
import arrow.core.raise.context.withError
import dev.zacsweers.metro.Inject
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.time.Clock

private val logger = KotlinLogging.logger {}

@Inject
class SaveDefaultOptionsUseCase(
    private val instanceRepository: InstanceRepository,
    private val instanceOptionsRepository: InstanceOptionsRepository,
) {
    context(_: Raise<InstanceOptionsError>)
    suspend operator fun invoke(sourceId: InstanceId, categories: Set<OptionsCategory>) {
        val instance = withError({ InstanceOptionsError.InstanceNotFound(it.id) }) {
            instanceRepository.findInstance(sourceId)
        }
        val source = OptionsSource.Instance(sourceId)
        instanceOptionsRepository.clearDefaultOptions()
        instanceOptionsRepository.readOptions(source)?.let { options ->
            val optionCategories = categories - FILE_OPTIONS_CATEGORIES
            if (optionCategories.isNotEmpty()) {
                instanceOptionsRepository.writeOptions(
                    OptionsSource.Default,
                    options.filterCategories(optionCategories)
                )
            }
            if (OptionsCategory.ResourcePacks in optionCategories) {
                instanceOptionsRepository.copyResourcePacks(source, OptionsSource.Default, options)
            }
        }
        if (OptionsCategory.ServerList in categories) {
            instanceOptionsRepository.copyFile(source, OptionsSource.Default, OptionsFile.ServersDat, overwrite = true)
        }
        if (OptionsCategory.Hotbars in categories) {
            instanceOptionsRepository.copyFile(source, OptionsSource.Default, OptionsFile.HotbarNbt, overwrite = true)
        }
        instanceOptionsRepository.saveDefaultOptions(
            DefaultOptions(
                categories = categories,
                sourceInstanceName = instance.name,
                gameVersion = instance.gameVersion,
                capturedAt = Clock.System.now(),
            )
        )
        logger.info { "Saved default options $categories from $sourceId" }
    }
}
