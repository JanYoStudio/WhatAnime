package pw.janyo.whatanime.config

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import pw.janyo.whatanime.module.*
import vip.mystery0.crashhandler.CrashHandler
import vip.mystery0.tools.ToolsClient

/**
 * Created by mystery0.
 */

class APP : Application() {
	override fun onCreate() {
		super.onCreate()
		startKoin {
			androidLogger()
			androidContext(this@APP)
			modules(listOf(databaseModule, networkModule, repositoryModule, viewModelModule, exoModule, mainActivityModule, historyActivityModule))
		}
		CrashHandler.config {
			it.setDirName("log")
			it.setFileNameSuffix("log")
			it.setDir(externalCacheDir!!)
		}.init()
		ToolsClient.initWithContext(this)
	}
}
