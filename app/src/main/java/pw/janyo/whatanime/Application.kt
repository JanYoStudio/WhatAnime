package pw.janyo.whatanime

import android.app.Application
import android.content.Context
import com.tencent.mmkv.MMKV
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import pw.janyo.whatanime.config.Configure
import pw.janyo.whatanime.module.moduleList
import pw.janyo.whatanime.utils.registerActivityLifecycle

class Application: Application() {
    override fun onCreate() {
        super.onCreate()
        context = this
        //配置Koin
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@Application)
            modules(moduleList())
        }
        registerActivityLifecycle()
        MMKV.initialize(this)
        if (Configure.lastVersion < 308) {
            //SP数据迁移到MMKV
            val sp = getSharedPreferences("configure", Context.MODE_PRIVATE)
            Configure.hideSex = sp.getBoolean("config_hide_sex", true)
            Configure.language = sp.getInt("config_language", 0)
            Configure.alreadyReadNotice = sp.getBoolean("config_read_notice", false)
        }
        Configure.lastVersion = BuildConfig.VERSION_CODE
    }
}