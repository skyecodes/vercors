package app.vercors.feature.game.backend.loader

import app.vercors.feature.game.api.dto.LoaderErrorData
import app.vercors.feature.game.api.dto.LoaderTypeData
import arrow.core.Either.Companion.catch
import arrow.core.raise.Raise
import arrow.core.raise.context.bind

internal val LoaderTypeData.key: String
    get() = when (this) {
        LoaderTypeData.Fabric -> FABRIC_LOADER_ID
        LoaderTypeData.Quilt -> QUILT_LOADER_ID
        LoaderTypeData.Forge -> FORGE_LOADER_ID
        LoaderTypeData.NeoForge -> NEOFORGE_LOADER_ID
    }

internal fun String.toLoaderTypeData(): LoaderTypeData = when (this) {
    FABRIC_LOADER_ID -> LoaderTypeData.Fabric
    QUILT_LOADER_ID -> LoaderTypeData.Quilt
    FORGE_LOADER_ID -> LoaderTypeData.Forge
    NEOFORGE_LOADER_ID -> LoaderTypeData.NeoForge
    else -> error("Unknown loader key: $this")
}

context(_: Raise<LoaderErrorData>)
internal suspend fun <T> loaderRequest(block: suspend () -> T): T =
    catch { block() }.mapLeft { LoaderErrorData(it::class.simpleName ?: "unknown", it.message) }.bind()
