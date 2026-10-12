package app.vercors.website.components.widgets

import androidx.compose.runtime.Composable
import app.vercors.website.DarkModeOnlyStyle
import app.vercors.website.LightModeOnlyStyle
import app.vercors.website.ScreenshotStyle
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.attrsModifier
import com.varabyte.kobweb.silk.components.graphics.Image
import com.varabyte.kobweb.silk.style.toModifier

// Both variants are rendered so the static export shows the right one before JS loads; lazy keeps the hidden one unfetched
@Composable
fun Screenshot(name: String, description: String, modifier: Modifier = Modifier) {
    val base = ScreenshotStyle.toModifier().attrsModifier { attr("loading", "lazy") }.then(modifier)
    Image("/images/screenshot_$name.png", description, DarkModeOnlyStyle.toModifier().then(base))
    Image("/images/screenshot_${name}_light.png", description, LightModeOnlyStyle.toModifier().then(base))
}
