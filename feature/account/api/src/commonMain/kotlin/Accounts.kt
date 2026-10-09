package app.vercors.feature.account.api

import app.vercors.shared.api.ApiV1
import io.ktor.utils.io.*
import opensavvy.spine.api.DynamicResource
import opensavvy.spine.api.StaticResource

object Accounts : StaticResource<ApiV1>("account", ApiV1) {
    object AccountId : DynamicResource<Accounts>("accountId", Accounts) {
        val avatar by get("avatar").response<ByteReadChannel>()

        val head by get("head").response<ByteReadChannel>()

        val body by get("body").response<ByteReadChannel>()
    }
}
