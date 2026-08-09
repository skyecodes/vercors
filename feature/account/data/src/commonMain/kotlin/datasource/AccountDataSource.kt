package app.vercors.feature.account.data.datasource

import app.vercors.core.filesystem.FileStore
import app.vercors.core.filesystem.FileStoreFactory
import app.vercors.feature.account.data.dto.AccountData
import app.vercors.shared.data.AppPaths
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.serialization.builtins.ListSerializer

@SingleIn(AppScope::class)
@Inject
class AccountDataSource(
    appPaths: AppPaths,
    fileStoreFactory: FileStoreFactory,
) : FileStore<List<AccountData>> by fileStoreFactory(
    filePath = appPaths.accountsFile,
    serializer = ListSerializer(AccountData.serializer()),
    defaultProvider = { emptyList() }
)
