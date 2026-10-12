import com.varabyte.kobweb.gradle.application.util.configAsKobwebApplication
import kotlinx.html.link
import kotlinx.html.script
import kotlinx.html.style
import kotlinx.html.unsafe

plugins {
    id("app.vercors.website")
    alias(libs.plugins.kobweb.application)
    alias(libs.plugins.kobwebx.markdown)
}

group = "app.vercors.website"
version = property("appVersion") as String

kobweb {
    app {
        index {
            description.set("A modern Minecraft launcher with mod support and much more!")
            faviconPath.set("/favicon.svg")
            head.add {
                link(rel = "icon", href = "/favicon.ico") { attributes["sizes"] = "32x32" }
                // WOFF2 copies of core/ui's Inter TTFs, regenerated with `woff2_compress` when those change
                val fonts = listOf("Inter" to "Inter.woff2", "Inter Tight" to "InterTight.woff2")
                fonts.forEach { (_, file) ->
                    link(rel = "preload", href = "/fonts/$file") {
                        attributes["as"] = "font"
                        attributes["type"] = "font/woff2"
                        attributes["crossorigin"] = ""
                    }
                }
                style {
                    unsafe {
                        raw(fonts.joinToString("\n") { (family, file) ->
                            "@font-face { font-family: \"$family\"; src: url(\"/fonts/$file\") format(\"woff2\"); " +
                                    "font-weight: 100 900; font-display: swap; }"
                        })
                    }
                }
                // Swaps the exported color mode class before first paint; must match initColorMode in AppEntry.kt
                script {
                    unsafe {
                        raw(
                            """
                            (function () {
                                var saved = null;
                                try { saved = localStorage.getItem("website:colorMode"); } catch (e) {}
                                var dark = saved ? saved === "DARK" : matchMedia("(prefers-color-scheme: dark)").matches;
                                var classes = document.documentElement.classList;
                                classes.remove(dark ? "silk-light" : "silk-dark");
                                classes.add(dark ? "silk-dark" : "silk-light");
                            })();
                            """.trimIndent()
                        )
                    }
                }
            }
        }
    }
    markdown {
        defaultLayout.set(".components.layouts.MarkdownLayout")
        process.set { entries ->
            fun String.quoted(): String = "\"" + replace("\\", "\\\\").replace("\"", "\\\"").replace("$", "\\$") + "\""
            val posts = entries
                .filter { it.filePath.startsWith("blog/") }
                .map { entry ->
                    fun field(key: String): String =
                        entry.frontMatter[key]?.singleOrNull()
                            ?: error("Blog post ${entry.filePath} must set \"$key\" in front matter")
                    listOf(entry.route, field("title"), field("date"), field("description"))
                }
                .sortedByDescending { it[2] }
            generateKotlin(
                "app/vercors/website/blog/BlogPosts.kt",
                buildString {
                    appendLine("package app.vercors.website.blog")
                    appendLine()
                    appendLine("val BLOG_POSTS: List<BlogPost> = listOf(")
                    posts.forEach { (route, title, date, description) ->
                        appendLine("    BlogPost(${route.quoted()}, ${title.quoted()}, ${date.quoted()}, ${description.quoted()}),")
                    }
                    appendLine(")")
                }
            )
        }
    }
}

// Screenshots and the logo are copied from the root public/ folder instead of being duplicated in git.
// They go into a resource source dir, not jsProcessResources: kobweb export only reads public/ from source dirs.
val syncSharedResources = tasks.register<Sync>("syncSharedResources") {
    into(layout.buildDirectory.dir("generated/sharedResources"))
    dependsOn(":app:launcher-demo:jsBrowserDistribution")
    from(rootProject.layout.projectDirectory.dir("app/launcher-demo/build/dist/js/productionExecutable")) {
        // The standalone dev page, source maps, and skiko's unbundled copies (the bundle loads the hashed .wasm)
        exclude("index.html", "*.map", "*.mjs", "skiko.wasm")
        into("public/demo")
    }
    from(rootProject.file("public/screenshots")) {
        into("public/screenshots")
    }
    from(rootProject.file("public/logo.ico")) {
        rename { "favicon.ico" }
        into("public")
    }
    // The browser tab follows the OS color scheme, not the site's toggle, so the favicon switches to the latte mauve itself
    from(rootProject.file("public/logo.svg")) {
        rename { "favicon.svg" }
        into("public")
        filter { line ->
            line.replace(
                "</svg>",
                "<style>@media (prefers-color-scheme: light) { polygon { fill: #8839ef } }</style></svg>",
            )
        }
    }
}

kotlin {
    configAsKobwebApplication("website")

    sourceSets {
        jsMain {
            resources.srcDir(syncSharedResources)
            dependencies {
                implementation(libs.compose.runtime)
                implementation(libs.compose.html.core)
                implementation(libs.kobweb.core)
                implementation(libs.kobweb.silk)
                implementation(libs.silk.icons.fa)
                implementation(libs.kobwebx.markdown)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)
            }
        }
    }
}
