package app.vercors.feature.account.command

import app.vercors.feature.account.domain.usecase.ObserveAccountListUseCase
import app.vercors.shared.command.AbstractCommand
import app.vercors.shared.command.CommandError
import app.vercors.shared.command.plural
import arrow.core.raise.Raise
import arrow.core.raise.context.bind
import arrow.core.raise.context.withError
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.mordant.rendering.BorderType
import com.github.ajalt.mordant.table.table
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import opensavvy.state.arrow.toEither
import opensavvy.state.coroutines.now

@ContributesIntoSet(AppScope::class)
class AccountsCommand(
    private val observeAccountList: ObserveAccountListUseCase
) : AbstractCommand("accounts") {

    context(_: Raise<CommandError>)
    override suspend fun execute() {
        val accountList = withError({ CommandError("Could not retrieve account list") }) {
            observeAccountList().now().toEither().bind()
        }
        if (accountList.isEmpty()) {
            echo("No account found. Login to your Microsoft account using 'vercors login'.")
        } else {
            echo("Found ${accountList.size} account${plural(accountList)}:")
            echo(table {
                borderType = BorderType.SQUARE_DOUBLE_SECTION_SEPARATOR
                header { row("Username", "UUID", "Active") }
                body {
                    accountList.forEach {
                        row(
                            it.username,
                            it.id.toHexString(),
                            it.id == accountList.first().id
                        )
                    }
                }
            })
        }
    }

    override fun help(context: Context): String = "Lists existing accounts"
}
