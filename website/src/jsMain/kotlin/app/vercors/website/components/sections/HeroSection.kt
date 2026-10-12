package app.vercors.website.components.sections

import androidx.compose.runtime.Composable
import app.vercors.website.*
import app.vercors.website.components.widgets.LiveDemo
import app.vercors.website.components.widgets.Logo
import app.vercors.website.data.Platform
import com.varabyte.kobweb.compose.css.TextAlign
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.icons.fa.FaDownload
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobweb.silk.style.toModifier
import kotlinx.browser.document
import org.jetbrains.compose.web.css.cssRem
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.P

@Composable
fun HeroSection(platform: Platform) {
    Column(
        Modifier.fillMaxWidth().gap(2.cssRem).margin(top = 2.cssRem),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Logo(Modifier.size(6.cssRem))
        H1(HeadlineTextStyle.toModifier().textAlign(TextAlign.Center).margin(0.cssRem).toAttrs()) {
            SpanText("A modern Minecraft launcher with ")
            SpanText("mod support", Modifier.color(SiteColors.primary))
        }
        P(
            SubheadlineTextStyle.toModifier().textAlign(TextAlign.Center).maxWidth(40.cssRem).margin(0.cssRem).toAttrs()
        ) {
            SpanText("Install mods and modpacks from CurseForge and Modrinth, keep all your instances organized, from a fast and modern app.")
        }
        HeroDownloadButton(platform)
        LiveDemo(
            "home",
            "$SITE_NAME home screen",
            Modifier.margin(top = 1.cssRem).width(100.percent).maxWidth(64.cssRem),
        )
    }
}

@Composable
private fun HeroDownloadButton(platform: Platform) {
    Button(
        onClick = { document.getElementById(DOWNLOAD_SECTION_ID)?.scrollIntoView() },
        Modifier.padding(leftRight = 1.5.cssRem),
        variant = PrimaryButtonVariant,
    ) {
        FaDownload(Modifier.margin(right = 0.5.cssRem))
        SpanText(platform.os?.let { "Download for ${it.displayName}" } ?: "Download")
    }
}
