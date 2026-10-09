package app.vercors.feature.theme.domain.usecase

import app.vercors.core.domain.ProgressiveStateFlow
import app.vercors.feature.theme.domain.model.ThemeCatalog
import app.vercors.feature.theme.domain.repository.ThemeRepository
import dev.zacsweers.metro.Inject

@Inject
class ObserveThemesUseCase(
    private val themeRepository: ThemeRepository,
) {
    operator fun invoke(): ProgressiveStateFlow<Nothing, ThemeCatalog> = themeRepository.catalog
}
