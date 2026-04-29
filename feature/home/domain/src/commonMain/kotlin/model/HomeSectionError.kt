package app.vercors.feature.home.domain.model

import app.vercors.feature.config.domain.model.ConfigurationError
import app.vercors.feature.project.domain.model.PlatformId
import kotlin.jvm.JvmInline

@JvmInline
value class HomeConfigurationError(val cause: ConfigurationError)

sealed interface HomeSectionError {
    @JvmInline
    value class PlatformNotFound(val platformId: PlatformId) : HomeSectionError

    data class PlatformError(val error: String, val description: String?) : HomeSectionError
}
