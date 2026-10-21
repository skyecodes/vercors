package app.vercors.feature.instance.domain.usecase

import app.vercors.feature.instance.domain.model.Instance
import app.vercors.feature.instance.domain.model.options.InstanceOptionsError
import app.vercors.feature.instance.domain.model.options.OptionsCopyResult
import app.vercors.feature.instance.domain.model.options.OptionsSource
import app.vercors.feature.instance.domain.repository.InstanceOptionsRepository
import arrow.core.raise.Raise
import arrow.core.raise.context.withError
import dev.zacsweers.metro.Inject
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

// Runs at first launch rather than creation, so it layers over options.txt files that pack overrides extracted
@Inject
class ApplyDefaultOptionsUseCase(
    private val instanceOptionsRepository: InstanceOptionsRepository,
    private val copyOptions: CopyOptionsUseCase,
    private val updateInstance: UpdateInstanceUseCase,
) {
    context(_: Raise<InstanceOptionsError>)
    suspend operator fun invoke(instance: Instance): OptionsCopyResult? {
        if (instance.defaultOptionsApplied) return null
        val defaultOptions = instanceOptionsRepository.getDefaultOptions()
        val result = defaultOptions?.takeIf { it.autoApply }?.let {
            logger.info { "Applying default options to ${instance.id}" }
            copyOptions(OptionsSource.Default, instance.id, it.categories, overwriteFiles = false)
        }
        withError({ _: Any -> InstanceOptionsError.WriteFailed }) {
            updateInstance(instance.id) { it.copy(defaultOptionsApplied = true) }
        }
        return result
    }
}
