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
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import pw.janyo.whatanime.BuildConfig
import pw.janyo.whatanime.R
import pw.janyo.whatanime.module.*
import vip.mystery0.crashhandler.CrashHandler
import vip.mystery0.logs.Logs
import vip.mystery0.tools.ToolsClient
import vip.mystery0.tools.context
import vip.mystery0.tools.utils.toastLong
import java.io.File

/**
 * Created by mystery0.
 */
class APP : Application() {

	override fun onCreate() {
		super.onCreate()
		startKoin {
			androidLogger(Level.ERROR)
			androidContext(this@APP)
			modules(listOf(appModule, databaseModule, networkModule, repositoryModule, viewModelModule, exoModule, mainActivityModule, historyActivityModule))
		}
		CrashHandler.config {
			setFileNameSuffix("log")
			setDir(File(externalCacheDir, "log"))
		}.init()
		Logs.setConfig {
			it.commonTag = packageName
			it.isShowLog = BuildConfig.DEBUG
		}
		ToolsClient.initWithContext(this)
	}
}

val publicDeviceId: String
	@SuppressLint("HardwareIds")
	get() {
		return Settings.Secure.getString(context().contentResolver, Settings.Secure.ANDROID_ID)
	}

var connectServer: Boolean = false
var inBlackList: Boolean = false

fun Context.toBrowser(url: String) {
	try {
		val intent = Intent(ACTION_VIEW, Uri.parse(url)).apply {
			flags = FLAG_ACTIVITY_NEW_TASK
		}
		startActivity(intent)
	} catch (e: ActivityNotFoundException) {
		loadInCustomTabs(url)
	}
}

fun Context.loadInCustomTabs(url: String) {
	try {
		val builder = CustomTabsIntent.Builder()
		val intent = builder.build()
		intent.launchUrl(this, Uri.parse(url))
	} catch (e: Exception) {
		toastLong(R.string.hint_no_browser)
	}
}