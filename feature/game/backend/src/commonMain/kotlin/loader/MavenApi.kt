package app.vercors.feature.game.backend.loader

import app.vercors.feature.game.backend.loader.dto.MavenVersionsResponse
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path

interface MavenApi {
    @GET("api/maven/versions/releases/{artifactPath}")
    suspend fun getReleaseVersions(
        @Path("artifactPath", encoded = true) artifactPath: String
    ): LoaderResponse<MavenVersionsResponse>
}
