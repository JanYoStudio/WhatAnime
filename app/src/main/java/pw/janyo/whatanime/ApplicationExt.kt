package pw.janyo.whatanime

import android.annotation.SuppressLint
import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.provider.Settings
import androidx.browser.customtabs.CustomTabsIntent
import org.koin.java.KoinJavaComponent
import pw.janyo.whatanime.base.BaseComposeActivity
import androidx.core.net.toUri

@SuppressLint("StaticFieldLeak")
internal lateinit var context: Context

//设备id
val publicDeviceId: String
    @SuppressLint("HardwareIds")
    get() = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

//应用名称
val appName: String
    get() = context.getString(R.string.app_name)

//应用包名
const val packageName: String = BuildConfig.APPLICATION_ID

//版本名称
const val appVersionName: String = BuildConfig.VERSION_NAME

//版本号
const val appVersionCode: String = BuildConfig.VERSION_CODE.toString()
const val appVersionCodeNumber: Long = BuildConfig.VERSION_CODE.toLong()

fun BaseComposeActivity.toCustomTabs(url: String) {
    if (url.isBlank()) {
        throw IllegalArgumentException("url is blank")
    }
    try {
        val builder = CustomTabsIntent.Builder()
        val intent = builder.build()
        intent.launchUrl(this, Uri.parse(url))
    } catch (e: Exception) {
        loadInBrowser(url)
    }
}

fun BaseComposeActivity.loadInBrowser(url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        R.string.hint_no_browser.toast(true)
    }
}

@Suppress("DEPRECATION")
fun isOnline(): Boolean {
    val connectivityManager =
        KoinJavaComponent.get<ConnectivityManager>(ConnectivityManager::class.java)
    val networkInfo = connectivityManager.activeNetworkInfo
    return networkInfo?.isConnected == true
}