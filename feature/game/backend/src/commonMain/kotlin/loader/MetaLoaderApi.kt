package app.vercors.feature.game.backend.loader

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path
import kotlinx.serialization.Serializable

interface MetaLoaderApi {
    @GET("versions/loader/{gameVersion}")
    suspend fun getLoaderVersions(@Path("gameVersion") gameVersion: String): List<MetaLoaderVersionEntry>
}

@Serializable
data class MetaLoaderVersionEntry(val loader: MetaLoaderVersionInfo)

@Serializable
data class MetaLoaderVersionInfo(val version: String, val stable: Boolean? = null) {
    // Quilt's meta API never sends `stable` (unlike Fabric's), fall back to a version-string heuristic
    val effectiveStable: Boolean get() = stable ?: !version.contains("-beta")
}
