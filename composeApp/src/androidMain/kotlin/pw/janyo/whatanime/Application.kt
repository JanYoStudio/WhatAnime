package pw.janyo.whatanime

import android.app.Application
import com.tencent.mmkv.MMKV
import multiplatform.network.cmptoast.AppContext
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import pw.janyo.whatanime.module.moduleList
import pw.janyo.whatanime.BuildConfig

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
        MMKV.initialize(this)
        Configure.lastVersion = BuildConfig.VERSION_CODE
        //每次启动都禁用调试模式
        Configure.debugMode = BuildConfig.DEBUG
    }
}