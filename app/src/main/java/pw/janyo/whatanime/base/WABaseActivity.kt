package pw.janyo.whatanime.base

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.os.LocaleList
import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import pw.janyo.whatanime.config.Configure
import vip.mystery0.tools.base.binding.BaseBindingActivity
import vip.mystery0.tools.utils.PackageTools
import java.util.*

abstract class WABaseActivity<B : ViewDataBinding>(@LayoutRes layoutId: Int?) : BaseBindingActivity<B>(layoutId) {
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
		return if (PackageTools.instance.isAfter(PackageTools.VERSION_O)) {
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