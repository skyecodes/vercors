package app.vercors.launcher.common.data.system

import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

internal actual fun copyToClipboard(text: String): CopyToClipboardResult = try {
    Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(text), null)
    CopyToClipboardResult.Success
} catch (e: Exception) {
    CopyToClipboardResult.Failure(e)
}
