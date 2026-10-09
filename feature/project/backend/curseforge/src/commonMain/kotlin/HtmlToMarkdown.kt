package app.vercors.feature.project.backend.curseforge

import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter

private val converter = FlexmarkHtmlConverter.builder().build()

// CurseForge only exposes descriptions as HTML; Markdown lets the client use a single renderer
internal fun String.toMarkdown(): String = converter.convert(this)
