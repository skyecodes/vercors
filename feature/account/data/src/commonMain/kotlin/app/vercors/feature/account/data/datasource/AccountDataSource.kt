package app.vercors.feature.account.data.datasource

import app.vercors.feature.account.data.dto.AccountListData
import app.vercors.feature.account.data.dto.toData
import app.vercors.feature.account.domain.model.AccountList
import app.vercors.shared.data.JsonFileStateFactory
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.io.files.Path
import org.koin.core.annotation.Single

private val logger = KotlinLogging.logger {}

internal const val ACCOUNT_LIST_FILE_NAME = "accounts.json"

@Single
internal class AccountDataSource(
    appPath: Path,
    jsonFileStateFactory: JsonFileStateFactory
) {
    private val accountsFile = Path(appPath, ACCOUNT_LIST_FILE_NAME)
    private val jsonFileState =
        jsonFileStateFactory(accountsFile, AccountListData.serializer()) { AccountList().toData() }

    val accountState = jsonFileState.state

    suspend fun saveToFile(value: AccountListData) = jsonFileState.write(value)
}
