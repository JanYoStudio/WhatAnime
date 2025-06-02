package pw.janyo.whatanime

import android.app.Application
import com.ctrip.flight.mmkv.initialize
import multiplatform.network.cmptoast.AppContext
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import pw.janyo.whatanime.module.moduleList

class Application : Application() {
    override fun onCreate() {
        super.onCreate()
        context = this
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@Application)
            modules(moduleList())
        }
        AppContext.apply { set(applicationContext) }
        initialize(this)
        Configure.lastVersion = BuildConfig.VERSION_CODE
        //每次启动都禁用调试模式
        Configure.debugMode = BuildConfig.DEBUG
    }
}