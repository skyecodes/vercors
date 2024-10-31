package app.vercors.launcher.account.domain.model

data class Account(
    val uuid: String,
    val username: String,
    val accessToken: String?,
    val refreshToken: String?
)