package app.vercors.website.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import app.vercors.website.SITE_NAME
import app.vercors.website.components.layouts.PageLayoutData
import app.vercors.website.components.sections.DownloadSection
import app.vercors.website.components.sections.FeaturesSection
import app.vercors.website.components.sections.HeroSection
import app.vercors.website.data.*
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.gap
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.data.add
import com.varabyte.kobweb.core.init.InitRoute
import com.varabyte.kobweb.core.init.InitRouteContext
import com.varabyte.kobweb.core.layout.Layout
import org.jetbrains.compose.web.css.cssRem

@InitRoute
fun initHomePage(ctx: InitRouteContext) {
    ctx.data.add(PageLayoutData(SITE_NAME))
}

@Page
@Layout(".components.layouts.PageLayout")
@Composable
fun HomePage() {
    val (releaseState, setReleaseState) = remember { mutableStateOf<ReleaseState>(ReleaseState.Loading) }
    val (platform, setPlatform) = remember { mutableStateOf(Platform(os = null, arch = Arch.X64)) }

    LaunchedEffect(Unit) {
        setPlatform(detectPlatform())
    }
    LaunchedEffect(Unit) {
        val release = runCatching { fetchLatestRelease() }.getOrNull()
        setReleaseState(if (release != null) ReleaseState.Loaded(release) else ReleaseState.Unavailable)
    }

    Column(Modifier.fillMaxWidth().gap(4.cssRem)) {
        HeroSection(platform)
        FeaturesSection()
        DownloadSection(releaseState, platform)
    }
}
