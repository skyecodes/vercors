package app.vercors.feature.instance.domain.usecase

import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.repository.InstanceRepository
import dev.zacsweers.metro.Inject

/** Best-effort: never raises, just reports whether the icon was successfully downloaded and set. */
@Inject
class SetInstanceIconFromUrlUseCase(
    private val instanceRepository: InstanceRepository
) {
    suspend operator fun invoke(instanceId: InstanceId, url: String): Boolean =
        instanceRepository.setInstanceIconFromUrl(instanceId, url)
}
