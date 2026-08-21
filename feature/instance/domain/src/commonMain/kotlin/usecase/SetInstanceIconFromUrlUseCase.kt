package app.vercors.feature.instance.domain.usecase

import app.vercors.feature.instance.domain.model.Instance
import app.vercors.feature.instance.domain.repository.InstanceRepository
import dev.zacsweers.metro.Inject

/** Best-effort: never raises, just reports whether the icon was successfully downloaded and set. */
@Inject
class SetInstanceIconFromUrlUseCase(
    private val instanceRepository: InstanceRepository
) {
    suspend operator fun invoke(instance: Instance, url: String): Boolean =
        instanceRepository.setInstanceIconFromUrl(instance, url)
}
