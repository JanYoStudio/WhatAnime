package pw.janyo.whatanime.base

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.os.LocaleList
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.databinding.ViewDataBinding
import com.zyao89.view.zloading.ZLoadingDialog
import com.zyao89.view.zloading.Z_TYPE
import pw.janyo.whatanime.R
import pw.janyo.whatanime.config.Configure
import vip.mystery0.tools.base.binding.BaseBindingActivity
import vip.mystery0.tools.getTColor
import vip.mystery0.tools.utils.AndroidVersionCode
import vip.mystery0.tools.utils.sdkIsAfter
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

	fun buildZLoadingDialog(@StringRes resId: Int,
							type: Z_TYPE = Z_TYPE.DOUBLE_CIRCLE,
							cancelable: Boolean = false,
							accentColor: Int = getTColor(R.color.mColorPrimary)): ZLoadingDialog = ZLoadingDialog(this)
			.setLoadingBuilder(type)
			.setHintTextSize(16f)
			.setHintText(getString(resId))
			.setCancelable(cancelable)
			.setCanceledOnTouchOutside(cancelable)
			.setLoadingColor(accentColor)
			.setHintTextColor(accentColor)

	fun buildZLoadingDialog(text: String = "",
							type: Z_TYPE = Z_TYPE.DOUBLE_CIRCLE,
							cancelable: Boolean = false,
							accentColor: Int = getTColor(R.color.mColorPrimary)): ZLoadingDialog = ZLoadingDialog(this)
			.setLoadingBuilder(type)
			.setHintTextSize(16f)
			.setHintText(text)
			.setCancelable(cancelable)
			.setCanceledOnTouchOutside(cancelable)
			.setLoadingColor(accentColor)
			.setHintTextColor(accentColor)

	fun Throwable?.toast() = dispatch(this, false, this@WABaseActivity)
	fun Throwable?.toastLong() = dispatch(this, true, this@WABaseActivity)
	fun String?.toast() = newToast(this@WABaseActivity, this, Toast.LENGTH_SHORT)
	fun String?.toastLong() = newToast(this@WABaseActivity, this, Toast.LENGTH_LONG)

	private fun dispatch(throwable: Throwable?, isLong: Boolean, context: Context = vip.mystery0.tools.context()) {
		newToast(context, throwable?.message
				?: getString(R.string.hint_unknow_error), if (isLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT)
	}

	private fun newToast(context: Context, message: String?, length: Int) {
		Toast.makeText(context, message, length).show()
	}
}