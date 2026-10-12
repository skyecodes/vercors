package app.vercors.launcher.demo

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.map.Mapper
import coil3.network.ktor3.KtorNetworkFetcherFactory
import kotlinx.browser.document
import okio.Path
import org.w3c.dom.url.URL

// Fake icon paths only carry a file name, the file itself is bundled as a Compose resource
private object DemoIconPathMapper : Mapper<Path, String> {
    override fun map(data: Path, options: coil3.request.Options): String = demoIconUrl(data.name)
}

fun demoIconUrl(fileName: String): String =
    URL(DemoRes.getUri("files/icons/$fileName"), document.baseURI).href

fun demoImageLoader(context: PlatformContext): ImageLoader =
    ImageLoader.Builder(context)
        .components {
            add(DemoIconPathMapper)
            add(KtorNetworkFetcherFactory())
        }
        .build()
