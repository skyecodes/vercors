package app.vercors.website.components.layouts

import androidx.compose.runtime.Composable
import app.vercors.website.SiteColors
import app.vercors.website.formatDate
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.gap
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.core.layout.Layout
import com.varabyte.kobweb.core.rememberPageContext
import com.varabyte.kobweb.silk.components.navigation.Link
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobwebx.markdown.markdown
import org.jetbrains.compose.web.css.cssRem
import org.jetbrains.compose.web.dom.H1

@Composable
@Layout(".components.layouts.MarkdownLayout")
fun BlogPostLayout(content: @Composable () -> Unit) {
    val frontMatter = rememberPageContext().markdown!!.frontMatter
    val title = frontMatter["title"]?.singleOrNull().orEmpty()
    val date = frontMatter["date"]?.singleOrNull()

    Column(Modifier.gap(0.5.cssRem).margin(bottom = 2.cssRem)) {
        Link("/blog", "← All posts")
        H1(Modifier.margin(0.cssRem).toAttrs()) { SpanText(title) }
        if (date != null) {
            SpanText(formatDate(date), Modifier.color(SiteColors.subtext))
        }
    }
    content()
}
