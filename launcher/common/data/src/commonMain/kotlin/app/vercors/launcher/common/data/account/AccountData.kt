package app.vercors.launcher.common.data.account

import app.vercors.launcher.common.domain.account.model.Account
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
internal data class AccountData(
    val uuid: Uuid,
    val username: String,
    val accessToken: String,
    val refreshToken: String
)

internal fun AccountData.toDomain(): Account = Account(
    uuid = uuid,
    username = username,
    accessToken = accessToken,
    refreshToken = refreshToken
)

internal fun Account.toData(): AccountData = AccountData(
    uuid = uuid,
    username = username,
    accessToken = accessToken,
    refreshToken = refreshToken
)
