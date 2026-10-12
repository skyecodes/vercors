package app.vercors.website.components.layouts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import app.vercors.website.SITE_NAME
import app.vercors.website.components.sections.Footer
import app.vercors.website.components.sections.NavHeader
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.ColumnScope
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.PageContext
import com.varabyte.kobweb.core.data.getValue
import com.varabyte.kobweb.core.layout.Layout
import com.varabyte.kobweb.silk.style.CssStyle
import com.varabyte.kobweb.silk.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.style.toModifier
import kotlinx.browser.document
import org.jetbrains.compose.web.css.cssRem
import org.jetbrains.compose.web.css.vh

val PageContentStyle = CssStyle {
    base { Modifier.fillMaxWidth().padding(leftRight = 1.5.cssRem, top = 2.cssRem, bottom = 4.cssRem) }
    Breakpoint.MD { Modifier.maxWidth(72.cssRem) }
}

class PageLayoutData(val title: String)

@Composable
@Layout
fun PageLayout(ctx: PageContext, content: @Composable ColumnScope.() -> Unit) {
    val data = ctx.data.getValue<PageLayoutData>()
    LaunchedEffect(data.title) {
        document.title = if (data.title == SITE_NAME) SITE_NAME else "${data.title} - $SITE_NAME"
    }

    Column(
        Modifier
            .fillMaxWidth()
            .minHeight(100.vh),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        NavHeader()
        Column(PageContentStyle.toModifier().flexGrow(1)) {
            content()
        }
        Footer(Modifier.fillMaxWidth())
    }
}
