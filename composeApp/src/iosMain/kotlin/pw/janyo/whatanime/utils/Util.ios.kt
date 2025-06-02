package pw.janyo.whatanime.utils

import coil3.PlatformContext

actual fun isOnline(): Boolean = true

actual fun toCustomTabs(context: PlatformContext, url: String) = loadInBrowser(context, url)

actual fun loadInBrowser(context: PlatformContext, url: String) {
}

actual fun copyToClipboard(context: PlatformContext, text: String) {
}