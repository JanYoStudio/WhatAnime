package pw.janyo.whatanime.utils

import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.os.LocaleList
import java.util.*

class LanguageContextWrapper(base: Context?) : ContextWrapper(base) {
	companion object {
		@JvmStatic
		fun wrap(context: Context, newLocale: Locale): ContextWrapper {
			val configuration = context.resources.configuration
			val newContext = when {
				Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> {
					configuration.setLocale(newLocale)
					val localeLost = LocaleList(newLocale)
					LocaleList.setDefault(localeLost)
					configuration.locales = localeLost
					context.createConfigurationContext(configuration)
				}
				Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> {
					configuration.setLocale(newLocale)
					context.createConfigurationContext(configuration)
				}
				else -> context
			}
			return ContextWrapper(newContext)
		}
	}
}