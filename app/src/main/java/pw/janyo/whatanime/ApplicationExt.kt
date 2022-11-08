package pw.janyo.whatanime

import android.annotation.SuppressLint
import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.browser.customtabs.CustomTabsIntent
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import org.koin.java.KoinJavaComponent
import pw.janyo.whatanime.base.BaseComposeActivity
import pw.janyo.whatanime.config.Configure

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
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
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

fun registerAppCenter(application: Application) {
    if (Configure.allowSendCrashReport) {
        if (BuildConfig.DEBUG) {
            AppCenter.setLogLevel(Log.VERBOSE)
        }
        AppCenter.setUserId(publicDeviceId)
        AppCenter.start(
            application,
            "0d392422-670e-488b-b62b-b33cb2c15c3c",
            Analytics::class.java,
            Crashes::class.java
        )
    }
}

fun trackEvent(event: String, properties: Map<String, String>? = null) {
    if (AppCenter.isConfigured() && Configure.allowSendCrashReport) {
        Analytics.trackEvent(event, properties)
    }
}

fun trackError(error: Throwable) {
    if (AppCenter.isConfigured() && Configure.allowSendCrashReport) {
        Crashes.trackError(error)
    }
}