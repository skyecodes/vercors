package app.vercors.feature.game.api

import app.vercors.feature.game.api.dto.LoaderErrorData
import app.vercors.feature.game.api.dto.LoaderTypeData
import app.vercors.feature.game.api.dto.LoaderVersionData
import io.ktor.http.*
import opensavvy.spine.api.DynamicResource
import opensavvy.spine.api.StaticResource

object Loaders : StaticResource<Game.GameVersionId>("loaders", Game.GameVersionId) {
    val list by get()
        .failure<LoaderErrorData>(HttpStatusCode.InternalServerError)
        .response<List<LoaderTypeData>>()

    object LoaderId : DynamicResource<Loaders>("loaderId", Loaders) {
        val versions by get("versions")
            .failure<LoaderErrorData>(HttpStatusCode.InternalServerError)
            .response<List<LoaderVersionData>>()
    }
}
