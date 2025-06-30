package pw.janyo.whatanime.utils

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.ConnectivityManager
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import co.touchlab.kermit.Logger
import coil3.PlatformContext
import org.koin.java.KoinJavaComponent
import pw.janyo.whatanime.appName

@Suppress("DEPRECATION")
actual fun isOnline(): Boolean {
    val connectivityManager =
        KoinJavaComponent.get<ConnectivityManager>(ConnectivityManager::class.java)
    val networkInfo = connectivityManager.activeNetworkInfo
    return networkInfo?.isConnected == true
}

actual fun copyToClipboard(context: PlatformContext, text: String) {
    val clipboardManager =
        KoinJavaComponent.get<ClipboardManager>(ClipboardManager::class.java)
    val clipData = ClipData.newPlainText(appName, text)
    clipboardManager.setPrimaryClip(clipData)
}