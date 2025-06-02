package pw.janyo.whatanime.utils

import coil3.PlatformContext

expect fun isOnline(): Boolean

expect fun toCustomTabs(context: PlatformContext, url: String)

expect fun loadInBrowser(context: PlatformContext, url: String)

expect fun copyToClipboard(context: PlatformContext, text: String)