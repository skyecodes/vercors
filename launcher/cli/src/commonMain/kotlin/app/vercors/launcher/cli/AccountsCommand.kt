package app.vercors.launcher.cli

import app.vercors.core.domain.await
import app.vercors.core.domain.orThrow
import app.vercors.feature.account.domain.usecase.ObserveAccountListUseCase
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.mordant.rendering.BorderType
import com.github.ajalt.mordant.table.table

class AccountsCommand(private val observeAccountList: ObserveAccountListUseCase) : VercorsCommandBase("accounts") {
    override suspend fun execute() {
        val accountList = observeAccountList().await().orThrow()
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
