package pw.janyo.whatanime.utils

import coil3.PlatformContext
import multiplatform.network.cmptoast.ToastDuration
import multiplatform.network.cmptoast.showToast
import org.jetbrains.compose.resources.getString
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIPasteboard
import platform.UIKit.popoverPresentationController
import whatanime.composeapp.generated.resources.Res
import whatanime.composeapp.generated.resources.hint_copy_done

actual fun isOnline(): Boolean = true // TODO: Implement actual network check for iOS

actual suspend fun copyToClipboard(context: PlatformContext, text: String) {
    val pasteboard = UIPasteboard.generalPasteboard
    pasteboard.string = text
    showToast(getString(Res.string.hint_copy_done))
}

actual fun showSharePanel(context: PlatformContext, shareText: String) {
    val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController
    if (rootViewController != null) {
        val activityItems = listOf(shareText)
        val activityViewController = UIActivityViewController(
            activityItems = activityItems,
            applicationActivities = null,
        )
        activityViewController.popoverPresentationController?.sourceView = rootViewController.view
        rootViewController.presentViewController(
            activityViewController,
            animated = true,
            completion = null
        )
    } else {
        showToast(
            "could not find root view controller to present share sheet",
            duration = ToastDuration.Long
        )
    }
}