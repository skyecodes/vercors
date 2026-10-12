package app.vercors.website.components.widgets

import androidx.compose.runtime.Composable
import app.vercors.website.SITE_NAME
import app.vercors.website.SiteColors
import com.varabyte.kobweb.compose.dom.svg.Polygon
import com.varabyte.kobweb.compose.dom.svg.Svg
import com.varabyte.kobweb.compose.dom.svg.ViewBox
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.compose.ui.toAttrs

// Inlined from public/logo.svg so the fill can follow the theme's primary color
@Composable
fun Logo(modifier: Modifier = Modifier) {
    Svg(attrs = modifier.styleModifier { property("fill", SiteColors.primary) }.toAttrs {
        viewBox(ViewBox.sized(128))
        attr("role", "img")
        attr("aria-label", "$SITE_NAME logo")
    }) {
        Polygon {
            points(22 to 48, 64 to 14, 64 to 14.965, 22.596 to 48.4825)
            fillOpacity(0.2)
        }
        Polygon {
            points(106 to 48, 64 to 14, 64 to 14.965, 105.404 to 48.4825)
            fillOpacity(0.2)
        }
        Polygon {
            points(64 to 82, 106 to 48, 106 to 80, 64 to 114)
            fillOpacity(0.55)
        }
        Polygon {
            points(22 to 48, 64 to 82, 64 to 114, 22 to 80)
        }
    }
}
