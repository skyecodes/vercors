package app.vercors.feature.instance.domain.usecase

import app.vercors.feature.instance.domain.model.Instance
import app.vercors.feature.instance.domain.model.InstanceIconError
import app.vercors.feature.instance.domain.repository.InstanceRepository
import arrow.core.raise.Raise
import dev.zacsweers.metro.Inject

@Inject
class SetInstanceIconFromUrlUseCase(
    private val instanceRepository: InstanceRepository
) {
    context(_: Raise<InstanceIconError>)
    suspend operator fun invoke(instance: Instance, url: String): Unit =
        instanceRepository.setInstanceIconFromUrl(instance, url)
}
