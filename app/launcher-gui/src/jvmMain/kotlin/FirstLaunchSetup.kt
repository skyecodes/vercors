package app.vercors.launcher.gui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.rememberWindowState
import app.vercors.core.domain.ThemeColor
import app.vercors.core.domain.ThemeColors
import app.vercors.core.filesystem.ensureRootReachable
import app.vercors.core.filesystem.withAppFolderName
import app.vercors.core.ui.component.AppIconTextButton
import app.vercors.core.ui.component.AppText
import app.vercors.core.ui.component.AppTextField
import app.vercors.core.ui.theme.*
import com.composables.icons.lucide.*
import com.composeunstyled.theme.Theme
import dev.nucleusframework.application.DecoratedWindow
import dev.nucleusframework.application.NucleusApplicationScope
import dev.nucleusframework.core.runtime.NucleusApp
import io.github.vinceglb.filekit.dialogs.compose.rememberDirectoryPickerLauncher
import io.github.vinceglb.filekit.path
import kotlinx.io.files.Path
import org.jetbrains.compose.resources.stringResource

/** The app's default theme (Catppuccin Mocha / Mauve) — hardcoded rather than read through
 *  `ObserveThemesUseCase`, since this screen runs before the DI graph (and its theme repository) exists. */
private val SetupThemeColors = ThemeColors(
    background = ThemeColor.Argb(0xFF1E1E2EL),
    backgroundAlt = ThemeColor.Argb(0xFF181825L),
    onBackground = ThemeColor.Argb(0xFFCDD6F4L),
    primary = ThemeColor.Named("accent"),
    onPrimary = ThemeColor.Argb(0xFF11111BL),
    error = ThemeColor.Argb(0xFFF38BA8L),
    onError = ThemeColor.Argb(0xFF11111BL),
    success = ThemeColor.Argb(0xFFA6E3A1L),
    onSuccess = ThemeColor.Argb(0xFF11111BL),
    warn = ThemeColor.Argb(0xFFFAB387L),
    surface = ThemeColor.Argb(0xFF313244L),
    surface2 = ThemeColor.Argb(0xFF45475AL),
    surface3 = ThemeColor.Argb(0xFF585B70L),
)
private const val SetupAccentColor = 0xFFCBA6F7L // mauve

/**
 * Shown once, on first launch, before the DI graph exists — building the graph is what first writes to disk
 * (config.json, logs/), so the app root has to be chosen before that.
 */
@Composable
fun NucleusApplicationScope.FirstLaunchSetupWindow(
    defaultRoot: Path,
    onConfirm: (Path) -> Unit,
) {
    (AppTheme(themeColors = SetupThemeColors, accentColor = SetupAccentColor)) {
        var pathText by remember { mutableStateOf(defaultRoot.toString()) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        val locationUnreachableMessage = stringResource(Res.string.setupLocationUnreachable)

        val directoryPicker = rememberDirectoryPickerLauncher { directory ->
            val picked = directory?.path?.let(::Path) ?: return@rememberDirectoryPickerLauncher
            pathText = withAppFolderName(NucleusApp.appId, picked).toString()
            errorMessage = null
        }

        fun confirm() {
            val typed = pathText.trim().trimEnd('/', '\\').ifEmpty { defaultRoot.toString() }
            val chosen = withAppFolderName(NucleusApp.appId, Path(typed))
            if (ensureRootReachable(chosen)) {
                onConfirm(chosen)
            } else {
                pathText = chosen.toString()
                errorMessage = locationUnreachableMessage
            }
        }

        DecoratedWindow(
            onCloseRequest = ::exitApplication,
            title = stringResource(Res.string.setupWindowTitle),
            state = rememberWindowState(size = DpSize(560.dp, 380.dp)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Theme[colors][backgroundAlt])
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                AppText(text = stringResource(Res.string.setupTitle), style = Theme[textStyles][title])
                AppText(
                    text = stringResource(Res.string.setupDescription),
                    style = Theme[textStyles][label],
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    AppTextField(
                        initialValue = pathText,
                        onValueChange = { pathText = it; errorMessage = null },
                        modifier = Modifier.weight(1f),
                    )
                    AppIconTextButton(
                        onClick = directoryPicker::launch,
                        text = stringResource(Res.string.setupSelectFolder),
                        imageVector = Lucide.FolderOpen,
                    )
                    AppIconTextButton(
                        onClick = { pathText = defaultRoot.toString(); errorMessage = null },
                        text = stringResource(Res.string.setupReset),
                        imageVector = Lucide.RotateCcw,
                    )
                }

                errorMessage?.let {
                    AppText(text = it, color = Theme[colors][error])
                }

                Spacer(modifier = Modifier.weight(1f))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AppIconTextButton(
                        onClick = ::exitApplication,
                        text = stringResource(Res.string.setupCancelAndQuit),
                        imageVector = Lucide.LogOut,
                        backgroundColor = Theme[colors][error],
                        contentColor = Theme[colors][onError],
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    AppIconTextButton(
                        onClick = ::confirm,
                        text = stringResource(Res.string.setupConfirmAndStart),
                        imageVector = Lucide.Check,
                        backgroundColor = Theme[colors][primary],
                        contentColor = Theme[colors][onPrimary],
                    )
                }
            }
        }
    }
}
