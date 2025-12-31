package app.vercors.feature.account.data.dto

import app.vercors.feature.account.domain.model.Account
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
internal data class AccountData(
    val id: Uuid,
    val username: String,
    val accessToken: String,
    val refreshToken: String,
    val dateAdded: Instant,
    val dateSelected: Instant
)

internal fun AccountData.toDomain(): Account = Account(
    id = id,
    username = username,
    accessToken = accessToken,
    refreshToken = refreshToken,
    dateAdded = dateAdded,
    dateSelected = dateSelected
)

internal fun Account.toData(): AccountData = AccountData(
    id = id,
    username = username,
    accessToken = accessToken,
    refreshToken = refreshToken,
    dateAdded = dateAdded,
    dateSelected = dateSelected
)
