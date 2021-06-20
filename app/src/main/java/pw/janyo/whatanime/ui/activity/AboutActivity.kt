package pw.janyo.whatanime.ui.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.os.LocaleList
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import pw.janyo.whatanime.R
import pw.janyo.whatanime.config.Configure
import pw.janyo.whatanime.ui.fragment.AboutFragment
import vip.mystery0.tools.utils.AndroidVersionCode
import vip.mystery0.tools.utils.sdkIsAfter
import java.util.*

class AboutActivity : AppCompatActivity(R.layout.activity_about) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.beginTransaction()
            .replace(R.id.content_wrapper, AboutFragment())
            .commit()
        title = getString(R.string.title_activity_settings)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = title
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val language = Configure.language
        if (language == 0) {
            super.attachBaseContext(newBase)
            return
        }
        super.attachBaseContext(changeLanguage(newBase))
    }

    @SuppressLint("NewApi")
    private fun changeLanguage(context: Context): Context {
        return if (sdkIsAfter(AndroidVersionCode.VERSION_O)) {
            val newLocale: Locale = when (Configure.language) {
                1 -> Locale.SIMPLIFIED_CHINESE
                2 -> Locale.TRADITIONAL_CHINESE
                3 -> Locale.forLanguageTag("zh-Hant-HK")
                else -> Locale.getDefault()
            }
            val configuration = Resources.getSystem().configuration
            configuration.setLocale(newLocale)
            configuration.setLocales(LocaleList(newLocale))
            context.createConfigurationContext(configuration)
        } else {
            context
        }
    }
}