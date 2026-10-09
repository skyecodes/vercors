package app.vercors.feature.game.backend.loader

import app.vercors.feature.game.backend.loader.dto.MetaLoaderVersionEntry
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path

interface MetaLoaderApi {
    @GET("versions/loader/{gameVersion}")
    suspend fun getLoaderVersions(
        @Path("gameVersion") gameVersion: String
    ): LoaderResponse<List<MetaLoaderVersionEntry>>
}
