package app.vercors.launcher.common.domain.account.model

import kotlin.uuid.Uuid

data class Account(
    val uuid: Uuid,
    val username: String,
    val accessToken: String,
    val refreshToken: String,
)
