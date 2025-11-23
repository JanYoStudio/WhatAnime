package pw.janyo.whatanime.utils

import coil3.PlatformContext
import multiplatform.network.cmptoast.showToast
import org.jetbrains.compose.resources.getString
import whatanime.composeapp.generated.resources.Res
import whatanime.composeapp.generated.resources.hint_copy_done

expect fun isOnline(): Boolean

expect suspend fun copyToClipboard(context: PlatformContext, text: String)

suspend fun copyToClipboardThenToast(context: PlatformContext, text: String) {
    copyToClipboard(context,text)
    showToast(getString(Res.string.hint_copy_done))
}

expect fun showSharePanel(context: PlatformContext, shareText: String)