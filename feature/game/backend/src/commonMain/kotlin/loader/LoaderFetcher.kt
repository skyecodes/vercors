package app.vercors.feature.game.backend.loader

import app.vercors.feature.game.api.dto.LoaderErrorData
import app.vercors.feature.game.api.dto.LoaderTypeData
import app.vercors.feature.game.api.dto.LoaderVersionData
import arrow.core.Either.Companion.catch
import arrow.core.raise.Raise
import arrow.core.raise.context.bind

interface LoaderFetcher {
    context(_: Raise<LoaderErrorData>)
    suspend fun getVersions(gameVersion: String): List<LoaderVersionData>
}

internal val LoaderTypeData.key: String
    get() = when (this) {
        LoaderTypeData.Fabric -> "fabric"
        LoaderTypeData.Quilt -> "quilt"
        LoaderTypeData.Forge -> "forge"
        LoaderTypeData.NeoForge -> "neoforge"
    }

internal fun String.toLoaderTypeData(): LoaderTypeData = when (this) {
    "fabric" -> LoaderTypeData.Fabric
    "quilt" -> LoaderTypeData.Quilt
    "forge" -> LoaderTypeData.Forge
    "neoforge" -> LoaderTypeData.NeoForge
    else -> error("Unknown loader key: $this")
}

context(_: Raise<LoaderErrorData>)
internal suspend fun <T> loaderRequest(block: suspend () -> T): T =
    catch { block() }.mapLeft { LoaderErrorData(it::class.simpleName ?: "unknown", it.message) }.bind()
