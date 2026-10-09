package app.vercors.feature.launcher.domain.usecase

import app.vercors.feature.config.domain.repository.ConfigurationRepository
import app.vercors.shared.domain.model.NativeLibraryOptions
import app.vercors.shared.domain.model.NativeLibraryType
import app.vercors.shared.domain.usecase.DetectNativeLibraryPathUseCase
import dev.zacsweers.metro.Inject
import opensavvy.state.coroutines.now
import opensavvy.state.outcome.valueOrNull

@Inject
class ResolveNativeLibraryOptionsUseCase(
    private val configurationRepository: ConfigurationRepository,
    private val detectNativeLibraryPath: DetectNativeLibraryPathUseCase,
) {
    suspend operator fun invoke(instanceOverride: NativeLibraryOptions): NativeLibraryOptions {
        val configuration = configurationRepository.state.now().valueOrNull
        val base = instanceOverride.withFallback(configuration?.nativeLibraryOptions ?: NativeLibraryOptions())
        return base.copy(
            glfwPath = base.glfwPath?.takeIf(String::isNotBlank)
                ?: (base.useSystemGlfw == true).takeIf { it }?.let { detectNativeLibraryPath(NativeLibraryType.Glfw) },
            openAlPath = base.openAlPath?.takeIf(String::isNotBlank)
                ?: (base.useSystemOpenAl == true).takeIf { it }
                    ?.let { detectNativeLibraryPath(NativeLibraryType.OpenAl) },
        )
    }
}
