package app.vercors.feature.account.data.dto

import app.vercors.feature.account.domain.model.Account
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class AccountData(
    val id: Uuid,
    val username: String,
    val accessToken: String,
    val refreshToken: String,
    val dateAdded: Instant,
    val dateSelected: Instant
)

internal suspend fun AccountData.toDomain(decrypt: suspend (String) -> String): Account = Account(
    id = id,
    username = username,
    accessToken = decrypt(accessToken),
    refreshToken = decrypt(refreshToken),
    dateAdded = dateAdded,
    dateSelected = dateSelected
)

internal suspend fun Account.toData(encrypt: suspend (String) -> String): AccountData = AccountData(
    id = id,
    username = username,
    accessToken = encrypt(accessToken),
    refreshToken = encrypt(refreshToken),
    dateAdded = dateAdded,
    dateSelected = dateSelected
)
