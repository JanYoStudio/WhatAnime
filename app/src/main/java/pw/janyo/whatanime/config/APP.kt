package pw.janyo.whatanime.config

import android.annotation.SuppressLint
import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import android.provider.Settings
import androidx.browser.customtabs.CustomTabsIntent
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import com.oasisfeng.condom.CondomContext
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import pw.janyo.whatanime.BuildConfig
import pw.janyo.whatanime.R
import pw.janyo.whatanime.module.*
import vip.mystery0.tools.ToolsClient
import vip.mystery0.tools.context
import vip.mystery0.tools.utils.sp
import vip.mystery0.tools.utils.toastLong


/**
 * Created by mystery0.
 */
class APP : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@APP)
            modules(
                listOf(
                    appModule,
                    databaseModule,
                    networkModule,
                    repositoryModule,
                    viewModelModule,
                    exoModule,
                )
            )
        }
        Logger.addLogAdapter(object : AndroidLogAdapter(
            PrettyFormatStrategy.newBuilder()
                .showThreadInfo(BuildConfig.DEBUG)
                .tag(packageName)
                .build()
        ) {
            override fun isLoggable(priority: Int, tag: String?): Boolean = BuildConfig.DEBUG
        })
        ToolsClient.initWithContext(this)
        MMKV.initialize(CondomContext.wrap(this, "mmkv"))
        if (Configure.lastVersion < 308) {
            //SP数据迁移到MMKV
            val sp = sp("configure", Context.MODE_PRIVATE)
            Configure.hideSex = sp.getBoolean("config_hide_sex", true)
            Configure.language = sp.getInt("config_language", 0)
            Configure.alreadyReadNotice = sp.getBoolean("config_read_notice", false)
            Configure.lastVersion = BuildConfig.VERSION_CODE
        }
        if (Configure.lastVersion < BuildConfig.VERSION_CODE) {
            //重置云端压缩策略
            Configure.requestType = 0
            Configure.lastVersion = BuildConfig.VERSION_CODE
        }
    }
}

val publicDeviceId: String
    @SuppressLint("HardwareIds")
    get() {
        return Settings.Secure.getString(context().contentResolver, Settings.Secure.ANDROID_ID)
    }

var connectServer: Boolean = false
var inBlackList: Boolean = false
var useServerCompress: Boolean = true
var inChina: Boolean? = null
var debugMode = MutableStateFlow(false)

fun Context.toCustomTabs(url: String) {
    try {
        val builder = CustomTabsIntent.Builder()
        val intent = builder.build()
        intent.launchUrl(this, Uri.parse(url))
    } catch (e: Exception) {
        loadInBrowser(url)
    }
}

fun Context.loadInBrowser(url: String) {
    try {
        val intent = Intent(ACTION_VIEW, Uri.parse(url)).apply {
            flags = FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        toastLong(R.string.hint_no_browser)
    }
}

fun Application.setSecret(secret: String) {
    if (secret.isNotBlank()) {
        if (!AppCenter.isConfigured()) {
            AppCenter.start(this, secret, Analytics::class.java, Crashes::class.java)
            AppCenter.setUserId(publicDeviceId)
        }
        Configure.lastAppCenterSecret = secret
    }
}

fun trackEvent(name: String, properties: Map<String, String> = emptyMap()) {
    Analytics.trackEvent(name, properties)
}