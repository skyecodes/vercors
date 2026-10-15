package app.vercors.feature.instance.domain.usecase

import app.vercors.feature.instance.domain.model.options.DefaultOptions
import app.vercors.feature.instance.domain.repository.InstanceOptionsRepository
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow

@Inject
class ObserveDefaultOptionsUseCase(
    private val instanceOptionsRepository: InstanceOptionsRepository
) {
    operator fun invoke(): Flow<DefaultOptions?> = instanceOptionsRepository.observeDefaultOptions()
}
