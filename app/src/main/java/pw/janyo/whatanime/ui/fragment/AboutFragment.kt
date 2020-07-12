package pw.janyo.whatanime.ui.fragment

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.android.ext.android.inject
import pw.janyo.whatanime.R
import pw.janyo.whatanime.config.Configure
import pw.janyo.whatanime.config.publicDeviceId
import vip.mystery0.tools.base.BasePreferenceFragment
import vip.mystery0.tools.utils.AndroidVersionCode
import vip.mystery0.tools.utils.sdkIsAfter

class AboutFragment : BasePreferenceFragment(R.xml.pref_about) {
	private val clipboardManager: ClipboardManager by inject()
	private val languageArray by lazy { resources.getStringArray(R.array.language) }
	private val nightModeArray by lazy { resources.getStringArray(R.array.night_mode) }
	private val previewConfigArray by lazy { resources.getStringArray(R.array.preview_config_summary) }

	override fun onActivityCreated(savedInstanceState: Bundle?) {
		super.onActivityCreated(savedInstanceState)
		val deviceIdPreference: Preference = findPreferenceById(R.string.key_device_id)
		val hideSexPreference: CheckBoxPreference = findPreferenceById(R.string.key_hide_sex)
		val languagePreference: Preference = findPreferenceById(R.string.key_language)
		val nightModePreference: Preference = findPreferenceById(R.string.key_night_mode)
		val previewConfigPreference: Preference = findPreferenceById(R.string.key_preview_config)
		val useInAppImageSelectPreference: CheckBoxPreference = findPreferenceById(R.string.key_use_in_app_image_select)
		val cloudCompressPreference: CheckBoxPreference = findPreferenceById(R.string.key_cloud_compress)

		deviceIdPreference.summary = publicDeviceId
		languagePreference.summary = languageArray[Configure.language]
		nightModePreference.summary = nightModeArray[Configure.nightMode]
		previewConfigPreference.summary = previewConfigArray[Configure.previewConfig]
		if (sdkIsAfter(AndroidVersionCode.VERSION_Q)) {
			Configure.useInAppImageSelect = false
			useInAppImageSelectPreference.isEnabled = false
		}
		useInAppImageSelectPreference.isChecked = Configure.useInAppImageSelect
		cloudCompressPreference.isChecked = Configure.enableCloudCompress

		deviceIdPreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
			val clipData = ClipData.newPlainText(getString(R.string.app_name), deviceIdPreference.summary)
			clipboardManager.setPrimaryClip(clipData)
			toast(getString(R.string.hint_copy_device_id))
			true
		}
		hideSexPreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, _ ->
			Configure.hideSex = !hideSexPreference.isChecked
			true
		}
		languagePreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
			var select = Configure.language
			val activity = requireActivity()
			MaterialAlertDialogBuilder(activity)
					.setTitle(R.string.title_change_language)
					.setSingleChoiceItems(languageArray, select) { _, which ->
						select = which
					}
					.setPositiveButton(android.R.string.ok) { _, _ ->
						val needRestart = select != Configure.language
						Configure.language = select
						languagePreference.summary = languageArray[Configure.language]
						if (needRestart) {
							val intent = activity.packageManager.getLaunchIntentForPackage(activity.packageName)
							intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
							activity.startActivity(intent)
							activity.finish()
						}
					}
					.setNegativeButton(android.R.string.cancel, null)
					.show()
			true
		}
		nightModePreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
			var select = Configure.nightMode
			val activity = requireActivity()
			MaterialAlertDialogBuilder(activity)
					.setTitle(" ")
					.setSingleChoiceItems(nightModeArray, select) { _, which ->
						select = which
					}
					.setPositiveButton(android.R.string.ok) { _, _ ->
						val needRestart = select != Configure.nightMode
						Configure.nightMode = select
						nightModePreference.summary = nightModeArray[Configure.nightMode]
						if (needRestart) {
							val intent = activity.packageManager.getLaunchIntentForPackage(activity.packageName)
							intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
							activity.startActivity(intent)
							activity.finish()
						}
					}
					.setNegativeButton(android.R.string.cancel, null)
					.show()
			true
		}
		previewConfigPreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
			var select = Configure.previewConfig
			val activity = requireActivity()
			MaterialAlertDialogBuilder(activity)
					.setTitle(R.string.title_change_preview_config)
					.setSingleChoiceItems(R.array.preview_config, select) { _, which ->
						select = which
					}
					.setPositiveButton(android.R.string.ok) { _, _ ->
						Configure.previewConfig = select
						previewConfigPreference.summary = previewConfigArray[Configure.previewConfig]
					}
					.setNegativeButton(android.R.string.cancel, null)
					.show()
			true
		}
		useInAppImageSelectPreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, _ ->
			Configure.useInAppImageSelect = !useInAppImageSelectPreference.isChecked
			true
		}
		cloudCompressPreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, _ ->
			Configure.enableCloudCompress = !cloudCompressPreference.isChecked
			true
		}
	}
}