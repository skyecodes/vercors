package app.vercors.feature.instance.domain.usecase

import app.vercors.feature.instance.domain.model.options.InstanceOptionsError
import app.vercors.feature.instance.domain.repository.InstanceOptionsRepository
import arrow.core.raise.Raise
import dev.zacsweers.metro.Inject

@Inject
class ClearDefaultOptionsUseCase(
    private val instanceOptionsRepository: InstanceOptionsRepository
) {
    context(_: Raise<InstanceOptionsError>)
    suspend operator fun invoke(): Unit = instanceOptionsRepository.clearDefaultOptions()
}
