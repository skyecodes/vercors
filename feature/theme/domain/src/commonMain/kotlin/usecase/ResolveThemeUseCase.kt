package app.vercors.feature.theme.domain.usecase

import app.vercors.feature.theme.domain.repository.ThemeRepository
import dev.zacsweers.metro.Inject

@Inject
class ResolveThemeUseCase(
    private val repository: ThemeRepository,
) {
    operator fun invoke(themeId: String) = repository.resolveTheme(themeId)
}
