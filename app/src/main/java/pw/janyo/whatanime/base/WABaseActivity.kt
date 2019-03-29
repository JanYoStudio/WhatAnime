package pw.janyo.whatanime.base

import android.content.Context
import android.os.Build
import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import pw.janyo.whatanime.config.Configure
import pw.janyo.whatanime.utils.LanguageContextWrapper
import vip.mystery0.tools.base.binding.BaseBindingActivity
import java.util.*

abstract class WABaseActivity<B : ViewDataBinding>(@LayoutRes layoutId: Int?) : BaseBindingActivity<B>(layoutId) {
	override fun attachBaseContext(newBase: Context) {
		val newLocale: Locale = when (Configure.language) {
			1 -> Locale.SIMPLIFIED_CHINESE
			2 -> Locale.TRADITIONAL_CHINESE
			3 -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) Locale.forLanguageTag("zh-Hant-HK") else Locale.SIMPLIFIED_CHINESE
			else -> Locale.getDefault()
		}
		super.attachBaseContext(LanguageContextWrapper.wrap(newBase, newLocale))
	}
}