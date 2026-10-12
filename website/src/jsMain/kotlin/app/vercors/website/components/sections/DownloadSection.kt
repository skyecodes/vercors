package app.vercors.website.components.sections

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import app.vercors.website.*
import app.vercors.website.data.*
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.css.TextAlign
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.foundation.layout.Spacer
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.navigation.OpenLinkStrategy
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.icons.fa.FaApple
import com.varabyte.kobweb.silk.components.icons.fa.FaLinux
import com.varabyte.kobweb.silk.components.icons.fa.FaWindows
import com.varabyte.kobweb.silk.components.layout.SimpleGrid
import com.varabyte.kobweb.silk.components.layout.numColumns
import com.varabyte.kobweb.silk.components.navigation.Link
import com.varabyte.kobweb.silk.components.navigation.UndecoratedLinkVariant
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobweb.silk.style.toModifier
import org.jetbrains.compose.web.css.FlexWrap
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.cssRem
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.H3

const val DOWNLOAD_SECTION_ID = "download"

@Composable
fun DownloadSection(releaseState: ReleaseState, platform: Platform) {
    Column(
        Modifier.id(DOWNLOAD_SECTION_ID).fillMaxWidth().gap(1.5.cssRem).scrollMargin(top = 2.cssRem),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        H2(SectionTitleStyle.toModifier().margin(0.cssRem).toAttrs()) { SpanText("Download") }
        ReleaseInfo(releaseState)
        when (releaseState) {
            ReleaseState.Loading -> SpanText("Fetching the latest release…")
            ReleaseState.Unavailable -> DownloadUnavailable()
            is ReleaseState.Loaded -> DownloadOptions(releaseState.release, platform)
        }
    }
}

@Composable
private fun ReleaseInfo(releaseState: ReleaseState) {
    val release = (releaseState as? ReleaseState.Loaded)?.release ?: return
    val info = buildList {
        add("v${release.version}")
        if (release.isPrerelease) add("Pre-release")
        release.publishedAt?.let { add(formatDate(it)) }
    }.joinToString(" / ")
    Link(
        release.url,
        "$info / Release notes",
        Modifier.color(SiteColors.subtext).fontSize(0.9.cssRem).textAlign(TextAlign.Center),
        variant = UndecoratedLinkVariant,
        openExternalLinksStrategy = OpenLinkStrategy.IN_NEW_TAB,
    )
}

@Composable
private fun DownloadUnavailable() {
    SpanText("The latest release couldn't be loaded.")
    Link(GITHUB_RELEASES_URL, "Browse all releases on GitHub", openExternalLinksStrategy = OpenLinkStrategy.IN_NEW_TAB)
}

@Composable
private fun DownloadOptions(release: Release, platform: Platform) {
    val (selectedOs, setSelectedOs) = remember(platform.os) { mutableStateOf(platform.os ?: Os.Windows) }
    val recommended = release.downloads.recommendedFor(platform)
    val forOs = release.downloads.filter { it.os == selectedOs }

    Row(Modifier.gap(0.5.cssRem).flexWrap(FlexWrap.Wrap), horizontalArrangement = Arrangement.Center) {
        Os.entries.forEach { os ->
            Button(
                onClick = { setSelectedOs(os) },
                Modifier.gap(0.5.cssRem).padding(leftRight = 1.25.cssRem),
                variant = if (os == selectedOs) PrimaryButtonVariant else null,
            ) {
                os.icon(Modifier.margin(right = 0.5.cssRem))
                SpanText(os.displayName)
            }
        }
    }

    SimpleGrid(numColumns(base = 1, md = 2), Modifier.fillMaxWidth().gap(1.cssRem)) {
        DownloadKind.entries.forEach { kind ->
            Arch.entries.forEach { arch ->
                val options = forOs.filter { it.kind == kind && it.arch == arch }
                if (options.isNotEmpty()) {
                    DownloadCard(
                        title = "${kind.title} / ${arch.displayName}",
                        options = options,
                        recommended = recommended
                    )
                }
            }
        }
    }
}

@Composable
private fun DownloadCard(
    title: String,
    options: List<DownloadOption>,
    recommended: DownloadOption?,
) {
    Column(CardStyle.toModifier().gap(0.25.cssRem)) {
        H3(Modifier.margin(top = 0.cssRem, bottom = 0.5.cssRem).fontSize(1.2.cssRem).toAttrs()) {
            SpanText(title)
        }
        options.sortedBy { it.format.ordinal }.forEach { option ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .gap(0.75.cssRem)
                    .padding(topBottom = 0.4.cssRem)
                    .borderTop(1.px, LineStyle.Solid, SiteColors.surface),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Row(Modifier.gap(0.5.cssRem), verticalAlignment = Alignment.CenterVertically) {
                        SpanText(
                            option.format.label,
                            Modifier.fontWeight(FontWeight.SemiBold),
                        )
                        if (option === recommended) {
                            SpanText("Recommended", Modifier.fontSize(0.75.cssRem).color(SiteColors.success))
                        }
                    }
                    SpanText(option.format.description, Modifier.fontSize(0.85.cssRem).color(SiteColors.subtext))
                }
                Spacer()
                SpanText(option.size.toReadableSize(), Modifier.fontSize(0.85.cssRem).color(SiteColors.subtext))
                Link(option.url, "Download")
            }
        }
    }
}

@Composable
private fun Os.icon(modifier: Modifier) = when (this) {
    Os.Windows -> FaWindows(modifier)
    Os.MacOs -> FaApple(modifier)
    Os.Linux -> FaLinux(modifier)
}

private val DownloadKind.title: String
    get() = when (this) {
        DownloadKind.Gui -> "Launcher"
        DownloadKind.Cli -> "Command line"
    }
