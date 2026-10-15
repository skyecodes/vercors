package app.vercors.feature.project.domain.usecase

import app.vercors.feature.instance.domain.model.Instance
import app.vercors.feature.instance.domain.repository.InstanceRepository
import app.vercors.feature.project.domain.model.pack.LocalPack
import app.vercors.feature.project.domain.model.pack.matches
import app.vercors.feature.project.domain.repository.InstalledPackRepository
import dev.zacsweers.metro.Inject
import opensavvy.state.progressive.valueOrNull

@Inject
class FindLocalPackTargetsUseCase(
    private val instanceRepository: InstanceRepository,
    private val installedPackRepository: InstalledPackRepository,
) {
    suspend operator fun invoke(pack: LocalPack): List<Instance> =
        instanceRepository.instances.value.valueOrNull?.values.orEmpty()
            .mapNotNull { it.getOrNull() }
            .filter { installedPackRepository.get(it.id)?.matches(pack) == true }
            .sortedBy { it.name.lowercase() }
}
