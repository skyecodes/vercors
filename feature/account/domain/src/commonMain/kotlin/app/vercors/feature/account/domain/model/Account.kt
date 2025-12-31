package app.vercors.feature.account.domain.model

import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

data class Account(
    val id: AccountId,
    val username: String,
    val accessToken: String,
    val refreshToken: String,
    val dateAdded: Instant = Clock.System.now(),
    val dateSelected: Instant = Clock.System.now()
)

typealias AccountId = Uuid
