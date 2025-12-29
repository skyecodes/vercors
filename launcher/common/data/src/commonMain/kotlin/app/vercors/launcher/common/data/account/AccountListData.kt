package app.vercors.launcher.common.data.account

import app.vercors.launcher.common.domain.account.model.AccountList
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
