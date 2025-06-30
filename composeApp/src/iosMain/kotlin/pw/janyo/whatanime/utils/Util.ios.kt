package pw.janyo.whatanime.utils

import coil3.PlatformContext
import platform.UIKit.UIPasteboard

actual fun isOnline(): Boolean = true // TODO: Implement actual network check for iOS

actual fun copyToClipboard(context: PlatformContext, text: String) {
    val pasteboard = UIPasteboard.generalPasteboard
    pasteboard.string = text
}