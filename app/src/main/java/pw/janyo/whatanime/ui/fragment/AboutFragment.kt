package pw.janyo.whatanime.ui.fragment

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.SwitchPreference
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.LibsBuilder
import pw.janyo.whatanime.R
import pw.janyo.whatanime.config.Configure
import vip.mystery0.tools.base.BasePreferenceFragment

class AboutFragment : BasePreferenceFragment(R.xml.pref_about) {
	override fun onActivityCreated(savedInstanceState: Bundle?) {
		super.onActivityCreated(savedInstanceState)
		val hideSexPreference: SwitchPreference = findPreferenceById(R.string.key_hide_sex)
		val openSourceLicenseAboutPreference: Preference = findPreferenceById(R.string.key_about_open_source_license)

		hideSexPreference.setOnPreferenceChangeListener { _, _ ->
			Configure.hideSex = !hideSexPreference.isChecked
			true
		}
		openSourceLicenseAboutPreference.setOnPreferenceClickListener {
			LibsBuilder()
					.withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
					.withAboutAppName(getString(R.string.app_name))
					.withAboutIconShown(false)
					.withAboutVersionShown(true)
					.withLicenseShown(true)
					.withLicenseDialog(true)
					.withShowLoadingProgress(true)
					.withLibraries(
							"DataBinding",
							"Lifecycles",
							"Matisse",
							"Tools",
							"Room",
							"ViewModel",
							"ZLoading")
					.start(activity)
			true
		}
	}
}