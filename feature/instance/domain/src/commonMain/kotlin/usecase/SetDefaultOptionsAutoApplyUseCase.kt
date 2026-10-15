package app.vercors.feature.instance.domain.usecase

import app.vercors.feature.instance.domain.model.options.InstanceOptionsError
import app.vercors.feature.instance.domain.repository.InstanceOptionsRepository
import arrow.core.raise.Raise
import arrow.core.raise.context.ensureNotNull
import dev.zacsweers.metro.Inject

@Inject
class SetDefaultOptionsAutoApplyUseCase(
    private val instanceOptionsRepository: InstanceOptionsRepository
) {
    context(_: Raise<InstanceOptionsError>)
    suspend operator fun invoke(autoApply: Boolean) {
        val defaultOptions = ensureNotNull(instanceOptionsRepository.getDefaultOptions()) {
            InstanceOptionsError.NoDefaultOptions
        }
        instanceOptionsRepository.saveDefaultOptions(defaultOptions.copy(autoApply = autoApply))
    }
}
