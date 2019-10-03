package pw.janyo.whatanime.config

import android.app.Application
import pw.janyo.whatanime.repository.local.db.DBHelper
import vip.mystery0.crashhandler.CrashHandler
import vip.mystery0.tools.ToolsClient

/**
 * Created by mystery0.
 */

class APP : Application() {
	override fun onCreate() {
		super.onCreate()
		CrashHandler.config {
			it.setDirName("log")
			it.setFileNameSuffix("log")
			it.setDir(externalCacheDir!!)
		}.initWithContext(this)
		DBHelper.init(this)
		ToolsClient.initWithContext(this)
	}
}
