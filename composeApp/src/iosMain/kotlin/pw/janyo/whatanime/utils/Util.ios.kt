package pw.janyo.whatanime.utils

import coil3.PlatformContext
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIPasteboard

actual fun isOnline(): Boolean = true // TODO: Implement actual network check for iOS

actual fun toCustomTabs(context: PlatformContext, url: String) = loadInBrowser(context, url)

actual fun loadInBrowser(context: PlatformContext, url: String) {
    val nsUrl = url.let { NSURL.URLWithString(it) } ?: return
    val application = UIApplication.sharedApplication
    if (application.canOpenURL(nsUrl)) {
        application.openURL(nsUrl)
    }
}

actual fun copyToClipboard(context: PlatformContext, text: String) {
    val pasteboard = UIPasteboard.generalPasteboard
    pasteboard.string = text
}