package app.vercors.shared.domain.usecase

import app.vercors.core.system.SystemService
import dev.zacsweers.metro.Inject

@Inject
class GetTotalPhysicalMemoryUseCase(
    private val systemService: SystemService
) {
    /** Total installed physical RAM, in bytes, or `null` if it can't be determined. */
    operator fun invoke(): Long? = systemService.totalPhysicalMemoryBytes
}
