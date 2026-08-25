package app.vercors.shared.domain.usecase

import app.vercors.shared.domain.model.NativeLibraryType
import app.vercors.shared.domain.repository.NativeLibraryRepository
import dev.zacsweers.metro.Inject

@Inject
class DetectNativeLibraryPathUseCase(
    private val nativeLibraryRepository: NativeLibraryRepository
) {
    suspend operator fun invoke(type: NativeLibraryType): String? =
        nativeLibraryRepository.detectSystemLibraryPath(type)
}
