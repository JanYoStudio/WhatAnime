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

/**
 * Created by mystery0.
 */
class APP : Application() {
	var connectServer: Boolean = false
	var inBlackList: Boolean = false

	override fun onCreate() {
		super.onCreate()
		startKoin {
			androidLogger(Level.ERROR)
			androidContext(this@APP)
			modules(listOf(databaseModule, networkModule, repositoryModule, viewModelModule, exoModule, mainActivityModule, historyActivityModule))
		}
		CrashHandler.config {
			it.setDirName("log")
			it.setFileNameSuffix("log")
			it.setDir(externalCacheDir!!)
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
		var deviceId = Configure.deviceID
		if (deviceId.isBlank())
			deviceId = Settings.Secure.getString(context().contentResolver, Settings.Secure.ANDROID_ID)
		return deviceId
	}