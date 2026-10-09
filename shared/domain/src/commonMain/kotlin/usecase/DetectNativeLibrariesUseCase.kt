package app.vercors.shared.domain.usecase

import app.vercors.shared.domain.model.DetectedNativeLibraries
import app.vercors.shared.domain.model.NativeLibraryType
import dev.zacsweers.metro.Inject

@Inject
class DetectNativeLibrariesUseCase(
    private val detectNativeLibraryPath: DetectNativeLibraryPathUseCase
) {
    suspend operator fun invoke(): DetectedNativeLibraries = DetectedNativeLibraries(
        glfwPath = detectNativeLibraryPath(NativeLibraryType.Glfw),
        openAlPath = detectNativeLibraryPath(NativeLibraryType.OpenAl)
    )
}
