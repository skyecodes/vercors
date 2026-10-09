package app.vercors.shared.domain.usecase

import app.vercors.core.system.SystemService
import dev.zacsweers.metro.Inject

private const val FALLBACK_TOTAL_MEMORY_MB = 16384
private const val BYTES_PER_MB = 1024L * 1024

@Inject
class GetTotalPhysicalMemoryMbUseCase(
    private val systemService: SystemService
) {
    operator fun invoke(): Int =
        systemService.totalPhysicalMemoryBytes?.let { (it / BYTES_PER_MB).toInt() } ?: FALLBACK_TOTAL_MEMORY_MB
}
