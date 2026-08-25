package app.vercors.feature.launcher.domain.usecase

import app.vercors.feature.config.domain.repository.ConfigurationRepository
import app.vercors.shared.domain.model.NativeLibraryOptions
import app.vercors.shared.domain.model.NativeLibraryType
import app.vercors.shared.domain.usecase.DetectNativeLibraryPathUseCase
import dev.zacsweers.metro.Inject
import opensavvy.state.progressive.ProgressiveOutcome

/** Resolves the effective native library options: the instance's own override if it has one, otherwise the
 *  globally-configured default. When a library is enabled but has no explicit path, resolves the real
 *  system-installed path so it can be passed to LWJGL instead of a bare library name. */
@Inject
class ResolveNativeLibraryOptionsUseCase(
    private val configurationRepository: ConfigurationRepository,
    private val detectNativeLibraryPath: DetectNativeLibraryPathUseCase,
) {
    suspend operator fun invoke(instanceOverride: NativeLibraryOptions?): NativeLibraryOptions {
        val base = instanceOverride ?: run {
            val configuration = (configurationRepository.state.value as? ProgressiveOutcome.Success)?.value
            configuration?.nativeLibraryOptions ?: NativeLibraryOptions()
        }
        return base.copy(
            glfwPath = base.glfwPath?.takeIf(String::isNotBlank)
                ?: base.useSystemGlfw.takeIf { it }?.let { detectNativeLibraryPath(NativeLibraryType.Glfw) },
            openAlPath = base.openAlPath?.takeIf(String::isNotBlank)
                ?: base.useSystemOpenAl.takeIf { it }?.let { detectNativeLibraryPath(NativeLibraryType.OpenAl) },
        )
    }
}
