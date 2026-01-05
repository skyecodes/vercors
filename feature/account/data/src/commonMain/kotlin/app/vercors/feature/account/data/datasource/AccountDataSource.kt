package app.vercors.feature.account.data.datasource

import app.vercors.feature.account.data.dto.AccountListData
import app.vercors.feature.account.data.dto.toData
import app.vercors.feature.account.domain.model.AccountList
import app.vercors.shared.data.JsonFileStoreFactory
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.io.files.Path

private val logger = KotlinLogging.logger {}

internal const val ACCOUNT_LIST_FILE_NAME = "accounts.json"

@SingleIn(AppScope::class)
@Inject
class AccountDataSource(
    appPath: Path,
    jsonFileStoreFactory: JsonFileStoreFactory
) {
    private val accountsFile = Path(appPath, ACCOUNT_LIST_FILE_NAME)
    private val store =
        jsonFileStoreFactory("accounts", accountsFile, AccountListData.serializer()) { AccountList().toData() }

    val accountState = store.state

    suspend fun saveToFile(value: AccountListData) = store.write(value)
}
