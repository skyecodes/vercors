package app.vercors.launcher.cli

import app.vercors.launcher.common.domain.account.usecase.ObserveAccountListUseCase
import app.vercors.launcher.common.domain.plural
import app.vercors.shared.domain.awaitValueOrThrow
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.mordant.rendering.BorderType
import com.github.ajalt.mordant.table.table

class AccountsCommand : VercorsCommandBase("accounts") {
    override suspend fun execute() {
        val observeAccountList = koin.get<ObserveAccountListUseCase>()
        val accountList = observeAccountList().awaitValueOrThrow()
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
                            it.id.toHexString(),
                            it.id == accountList.selected
                        )
                    }
                }
            })
        }
    }

    override fun help(context: Context): String = "Lists existing accounts"
}
