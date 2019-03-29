package pw.janyo.whatanime.ui.fragment

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import androidx.preference.SwitchPreference
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.LibsBuilder
import pw.janyo.whatanime.R
import pw.janyo.whatanime.config.Configure
import vip.mystery0.tools.base.BasePreferenceFragment

class AboutFragment : BasePreferenceFragment(R.xml.pref_about) {
	private val languageArray by lazy { resources.getStringArray(R.array.language) }

	override fun onActivityCreated(savedInstanceState: Bundle?) {
		super.onActivityCreated(savedInstanceState)
		val hideSexPreference: SwitchPreference = findPreferenceById(R.string.key_hide_sex)
		val languagePreference: Preference = findPreferenceById(R.string.key_language)
		val openSourceLicenseAboutPreference: Preference = findPreferenceById(R.string.key_about_open_source_license)

		languagePreference.summary = languageArray[Configure.language]

		hideSexPreference.setOnPreferenceChangeListener { _, _ ->
			Configure.hideSex = !hideSexPreference.isChecked
			true
		}
		languagePreference.setOnPreferenceClickListener {
			var select = Configure.language
			AlertDialog.Builder(activity!!)
					.setTitle(R.string.title_change_language)
					.setSingleChoiceItems(languageArray, select) { _, which ->
						select = which
					}
					.setPositiveButton(android.R.string.ok) { _, _ ->
						val needRestart = select != Configure.language
						Configure.language = select
						languagePreference.summary = languageArray[Configure.language]
						if (needRestart) {
							val intent = activity!!.packageManager.getLaunchIntentForPackage(activity!!.packageName)
							intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
							activity!!.startActivity(intent)
							activity!!.finish()
						}
					}
					.setNegativeButton(android.R.string.cancel, null)
					.show()
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