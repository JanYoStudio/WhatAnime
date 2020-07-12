package pw.janyo.whatanime.config

import android.annotation.SuppressLint
import android.app.Application
import android.provider.Settings
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import pw.janyo.whatanime.BuildConfig
import pw.janyo.whatanime.module.*
import vip.mystery0.crashhandler.CrashHandler
import vip.mystery0.logs.Logs
import vip.mystery0.tools.ToolsClient
import vip.mystery0.tools.context
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