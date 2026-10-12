package app.vercors.website.components.widgets

import androidx.compose.runtime.Composable
import app.vercors.website.DarkModeFadeStyle
import app.vercors.website.LightModeFadeStyle
import app.vercors.website.ScreenshotStyle
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.attrsModifier
import com.varabyte.kobweb.compose.ui.modifiers.width
import com.varabyte.kobweb.silk.components.graphics.Image
import com.varabyte.kobweb.silk.style.toModifier
import org.jetbrains.compose.web.css.percent

// Both variants are stacked so the static export shows the right one before JS loads and they cross-fade on toggle
@Composable
fun Screenshot(name: String, description: String, modifier: Modifier = Modifier) {
    val image = ScreenshotStyle.toModifier().width(100.percent).attrsModifier { attr("loading", "lazy") }
    Box(modifier) {
        Image("/screenshots/$name.webp", description, DarkModeFadeStyle.toModifier().then(image))
        Image("/screenshots/${name}_light.webp", description, LightModeFadeStyle.toModifier().then(image))
    }
}
