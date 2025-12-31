package app.vercors.feature.account.data.dto

import app.vercors.feature.account.domain.model.AccountList
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
internal data class AccountListData(
    val selected: Uuid?,
    val accounts: List<AccountData>
)

internal fun AccountListData.toDomain(): AccountList = AccountList(
    accounts = accounts.map { it.toDomain() },
    selected = selected
)

internal fun AccountList.toData(): AccountListData = AccountListData(
    accounts = accounts.map { it.toData() },
    selected = selected
)
