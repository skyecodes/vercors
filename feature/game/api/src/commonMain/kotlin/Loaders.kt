package app.vercors.feature.game.api

import app.vercors.feature.game.api.dto.LoaderErrorData
import app.vercors.feature.game.api.dto.LoaderTypeData
import app.vercors.feature.game.api.dto.LoaderVersionData
import io.ktor.http.*
import io.ktor.utils.io.*
import opensavvy.spine.api.*
import opensavvy.spine.api.Parameters

object Loaders : StaticResource<Game.GameVersionId>("loaders", Game.GameVersionId) {
    val list by get()
        .failure<LoaderErrorData>(HttpStatusCode.InternalServerError)
        .response<List<LoaderTypeData>>()

    object LoaderId : DynamicResource<Loaders>("loaderId", Loaders) {
        val versions by get("versions")
            .failure<LoaderErrorData>(HttpStatusCode.InternalServerError)
            .response<List<LoaderVersionData>>()

        val installUrl by get("install-url")
            .parameters(::InstallUrlParameters)
            .failure<LoaderErrorData>(HttpStatusCode.InternalServerError)
            .response<ByteReadChannel>()

        class InstallUrlParameters(data: ParameterStorage) : Parameters(data) {
            var loaderVersion: String? by parameter()
        }
    }
}
