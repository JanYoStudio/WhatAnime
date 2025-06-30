package pw.janyo.whatanime.utils

import coil3.PlatformContext

expect fun isOnline(): Boolean

expect fun copyToClipboard(context: PlatformContext, text: String)