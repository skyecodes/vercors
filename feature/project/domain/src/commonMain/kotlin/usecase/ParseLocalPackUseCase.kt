package app.vercors.feature.project.domain.usecase

import app.vercors.feature.instance.domain.model.localimport.LocalImportError
import app.vercors.feature.instance.domain.usecase.ParsePrismInstanceUseCase
import app.vercors.feature.project.domain.model.modpack.ModpackResolutionError
import app.vercors.feature.project.domain.model.pack.LocalPack
import app.vercors.feature.project.domain.model.pack.LocalPackError
import app.vercors.feature.project.domain.model.pack.VercorsPackError
import app.vercors.feature.project.domain.repository.InstancePackRepository
import app.vercors.feature.project.domain.repository.ModpackRepository
import arrow.core.raise.Raise
import arrow.core.raise.context.raise
import arrow.core.raise.recover
import dev.zacsweers.metro.Inject

// Vercors first since its own format is unambiguous, then Prism before .mrpack, matching PrismLauncher's order
@Inject
class ParseLocalPackUseCase(
    private val instancePackRepository: InstancePackRepository,
    private val parsePrismInstance: ParsePrismInstanceUseCase,
    private val modpackRepository: ModpackRepository,
) {
    context(_: Raise<LocalPackError>)
    suspend operator fun invoke(sourcePath: String): LocalPack =
        recover({
            LocalPack.Vercors(sourcePath, instancePackRepository.parseVercorsPack(sourcePath))
        }) { error: VercorsPackError ->
            when (error) {
                VercorsPackError.NotVercorsFormat -> parseAsPrism(sourcePath)
                VercorsPackError.ParseFailed -> raise(LocalPackError.ParseFailed)
                VercorsPackError.UnsupportedFormatVersion -> raise(LocalPackError.UnsupportedPackVersion)
            }
        }

    context(_: Raise<LocalPackError>)
    private suspend fun parseAsPrism(sourcePath: String): LocalPack =
        recover({ LocalPack.Prism(sourcePath, parsePrismInstance(sourcePath)) }) { error: LocalImportError ->
            if (error == LocalImportError.NotPrismFormat) parseAsMrpack(sourcePath)
            else raise(LocalPackError.ParseFailed)
        }

    context(_: Raise<LocalPackError>)
    private suspend fun parseAsMrpack(sourcePath: String): LocalPack =
        recover({
            LocalPack.Mrpack(sourcePath, modpackRepository.parseLocalModpack(sourcePath, MODRINTH_PLATFORM_ID))
        }) { _: ModpackResolutionError ->
            raise(LocalPackError.UnsupportedFormat)
        }
}
