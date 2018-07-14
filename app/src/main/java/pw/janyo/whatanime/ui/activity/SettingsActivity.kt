package pw.janyo.whatanime.ui.activity

import android.annotation.TargetApi
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceActivity
import android.preference.PreferenceFragment
import pw.janyo.whatanime.R

/**
 * A [PreferenceActivity] that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 *
 * See [Android Design: Settings](http://developer.android.com/design/patterns/settings.html)
 * for design guidelines and the [Settings API Guide](http://developer.android.com/guide/topics/ui/settings.html)
 * for more information on developing a Settings UI.
 */
class SettingsActivity : AppCompatPreferenceActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setupActionBar()
	}

	/**
	 * Set up the [android.app.ActionBar], if the API is available.
	 */
	private fun setupActionBar() {
		supportActionBar?.setDisplayHomeAsUpEnabled(true)
	}

	/**
	 * {@inheritDoc}
	 */
	override fun onIsMultiPane(): Boolean {
		return isXLargeTablet(this)
	}

	/**
	 * {@inheritDoc}
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	override fun onBuildHeaders(target: List<PreferenceActivity.Header>) {
//		loadHeadersFromResource(R.xml.pref_headers, target)
	}

	/**
	 * This method stops fragment injection in malicious applications.
	 * Make sure to deny any unknown fragments here.
	 */
	override fun isValidFragment(fragmentName: String): Boolean {
		return PreferenceFragment::class.java.name == fragmentName
//				|| GeneralPreferenceFragment::class.java.name == fragmentName
//				|| DataSyncPreferenceFragment::class.java.name == fragmentName
//				|| NotificationPreferenceFragment::class.java.name == fragmentName
	}

	companion object {
		/**
		 * Helper method to determine if the device has an extra-large screen. For
		 * example, 10" tablets are extra-large.
		 */
		private fun isXLargeTablet(context: Context): Boolean {
			return context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_XLARGE
		}
	}
}
