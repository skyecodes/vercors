package app.vercors.feature.game.api

import app.vercors.shared.api.ApiV1
import io.ktor.utils.io.*
import opensavvy.spine.api.StaticResource

object Game : StaticResource<ApiV1>("game", ApiV1) {
    val versions by get("versions").response<ByteReadChannel>()

    val runtimes by get("runtimes").response<ByteReadChannel>()
}
