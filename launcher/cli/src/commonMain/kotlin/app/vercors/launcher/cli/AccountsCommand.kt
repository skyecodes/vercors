package app.vercors.launcher.cli

import app.vercors.launcher.common.domain.account.usecase.GetAccountListUseCase
import app.vercors.launcher.common.domain.plural
import app.vercors.shared.domain.awaitValueOrThrow
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.mordant.rendering.BorderType
import com.github.ajalt.mordant.table.table

class AccountsCommand : VercorsCommandBase("accounts") {
    override suspend fun execute() {
        val getAccountList = koin.get<GetAccountListUseCase>()
        val accountList = getAccountList().awaitValueOrThrow()
        if (accountList.accounts.isEmpty()) {
            echo("No account found. Login to your Microsoft account using 'vercors login'.")
        } else {
            echo("Found ${accountList.accounts.size} account${plural(accountList.accounts)}:")
            echo(table {
                borderType = BorderType.SQUARE_DOUBLE_SECTION_SEPARATOR
                header { row("Username", "UUID", "Active") }
                body {
                    accountList.accounts.forEach {
                        row(
                            it.username,
                            it.uuid.toHexString(),
                            it.uuid == accountList.selected
                        )
                    }
                }
            })
        }
    }

    override fun help(context: Context): String = "Lists existing accounts"
}