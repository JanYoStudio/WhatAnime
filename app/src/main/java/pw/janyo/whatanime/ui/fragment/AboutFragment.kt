package pw.janyo.whatanime.ui.fragment

import android.content.Intent
import android.os.Bundle
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.LibsBuilder
import pw.janyo.whatanime.R
import pw.janyo.whatanime.config.Configure
import vip.mystery0.tools.base.BasePreferenceFragment

class AboutFragment : BasePreferenceFragment(R.xml.pref_about) {
	private val languageArray by lazy { resources.getStringArray(R.array.language) }
	private val nightModeArray by lazy { resources.getStringArray(R.array.night_mode) }

	override fun onActivityCreated(savedInstanceState: Bundle?) {
		super.onActivityCreated(savedInstanceState)
		val hideSexPreference: CheckBoxPreference = findPreferenceById(R.string.key_hide_sex)
		val languagePreference: Preference = findPreferenceById(R.string.key_language)
		val nightModePreference: Preference = findPreferenceById(R.string.key_night_mode)
		val useInAppImageSelect: CheckBoxPreference = findPreferenceById(R.string.key_use_in_app_image_select)
		val openSourceLicenseAboutPreference: Preference = findPreferenceById(R.string.key_about_open_source_license)

		languagePreference.summary = languageArray[Configure.language]
		nightModePreference.summary = nightModeArray[Configure.nightMode]

		hideSexPreference.setOnPreferenceChangeListener { _, _ ->
			Configure.hideSex = !hideSexPreference.isChecked
			true
		}
		languagePreference.setOnPreferenceClickListener {
			var select = Configure.language
			MaterialAlertDialogBuilder(activity!!)
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
		nightModePreference.setOnPreferenceClickListener {
			var select = Configure.nightMode
			MaterialAlertDialogBuilder(activity!!)
					.setTitle(" ")
					.setSingleChoiceItems(nightModeArray, select) { _, which ->
						select = which
					}
					.setPositiveButton(android.R.string.ok) { _, _ ->
						val needRestart = select != Configure.nightMode
						Configure.nightMode = select
						nightModePreference.summary = nightModeArray[Configure.language]
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
		useInAppImageSelect.setOnPreferenceChangeListener { _, _ ->
			Configure.useInAppImageSelect = !useInAppImageSelect.isChecked
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