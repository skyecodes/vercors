package app.vercors.feature.config.command

import app.vercors.feature.config.domain.usecase.ObserveConfigurationUseCase
import app.vercors.feature.config.domain.usecase.UpdateConfigurationUseCase
import app.vercors.shared.command.AbstractCommand
import app.vercors.shared.command.CommandError
import arrow.core.raise.Raise
import arrow.core.raise.context.bind
import arrow.core.raise.context.ensureNotNull
import arrow.core.raise.context.withError
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.arguments.validate
import com.github.ajalt.clikt.parameters.types.choice
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import opensavvy.state.arrow.toEither
import opensavvy.state.coroutines.now

private const val ACTION_GET = "get"
private const val ACTION_SET = "set"

private const val KEY_META_API_URL = "metaApiUrl"
private const val KEY_PARALLEL_DOWNLOADS = "parallelDownloads"
private const val KEY_PARALLEL_FILESYSTEM_OPERATIONS = "parallelFilesystemOperations"
private const val KEY_MENU_DRAWER_OPEN = "menuDrawerOpen"
private const val KEY_ANIMATIONS = "animations"
private const val KEY_PREFERRED_PLATFORM = "preferredPlatform"
private const val KEY_SELECTED_THEME_ID = "selectedThemeId"
private const val KEY_SELECTED_ACCENT = "selectedAccent"

@ContributesIntoSet(AppScope::class)
class ConfigCommand(
    private val observeConfiguration: ObserveConfigurationUseCase,
    private val updateConfiguration: UpdateConfigurationUseCase,
) : AbstractCommand("config") {
    val action by argument(help = "Action: get or set").choice(ACTION_GET, ACTION_SET)
    val key by argument(help = "Key").choice(
        KEY_META_API_URL,
        KEY_PARALLEL_DOWNLOADS,
        KEY_PARALLEL_FILESYSTEM_OPERATIONS,
        KEY_MENU_DRAWER_OPEN,
        KEY_ANIMATIONS,
        KEY_PREFERRED_PLATFORM,
        KEY_SELECTED_THEME_ID,
        KEY_SELECTED_ACCENT
    )
    val value by argument(help = "Value").optional().validate {
        require(it.isNotEmpty()) { "Value cannot be empty" }
    }

    context(_: Raise<CommandError>)
    override suspend fun execute() {
        when (action) {
            ACTION_GET -> {
                val configuration = withError({ CommandError("Could not load configuration") }) {
                    observeConfiguration().now().toEither().bind()
                }
                when (key) {
                    KEY_META_API_URL -> echo(configuration.metaApiUrl)
                    KEY_PARALLEL_DOWNLOADS -> echo(configuration.parallelDownloads)
                    KEY_PARALLEL_FILESYSTEM_OPERATIONS -> echo(configuration.parallelFilesystemOperations)
                    KEY_MENU_DRAWER_OPEN -> echo(configuration.menuDrawerOpen)
                    KEY_ANIMATIONS -> echo(configuration.animations)
                    KEY_PREFERRED_PLATFORM -> echo(configuration.preferredPlatform)
                    KEY_SELECTED_THEME_ID -> echo(configuration.selectedThemeId)
                    KEY_SELECTED_ACCENT -> echo(configuration.selectedAccent)
                }
            }

            ACTION_SET -> {
                val v = ensureNotNull(value) { CommandError("Value must be specified for set action") }
                when (key) {
                    KEY_META_API_URL -> updateConfiguration { copy(metaApiUrl = v) }
                    KEY_PARALLEL_DOWNLOADS -> updateConfiguration { copy(parallelDownloads = v.toInt()) }
                    KEY_PARALLEL_FILESYSTEM_OPERATIONS -> updateConfiguration { copy(parallelFilesystemOperations = v.toInt()) }
                    KEY_MENU_DRAWER_OPEN -> updateConfiguration { copy(menuDrawerOpen = v.toBoolean()) }
                    KEY_ANIMATIONS -> updateConfiguration { copy(animations = v.toBoolean()) }
                    KEY_PREFERRED_PLATFORM -> updateConfiguration { copy(preferredPlatform = v) }
                    KEY_SELECTED_THEME_ID -> updateConfiguration { copy(selectedThemeId = v) }
                    KEY_SELECTED_ACCENT -> updateConfiguration { copy(selectedAccent = v) }
                }
                echo("Configuration updated successfully")
            }
        }
    }

    override fun help(context: Context): String = "Read or update configuration"
}
