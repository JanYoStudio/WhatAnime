package pw.janyo.whatanime.fragment

import android.content.DialogInterface
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import com.google.android.material.textfield.TextInputLayout
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import androidx.core.widget.NestedScrollView
import androidx.appcompat.app.AlertDialog
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import pw.janyo.whatanime.R
import pw.janyo.whatanime.util.Settings

/**
 * Created by myste.
 */

class SettingsFragment : PreferenceFragment() {
	private var resultNumberPreference: Preference? = null
	private var similarityPreference: Preference? = null
	private var licensePreference: Preference? = null
	private var janyoInfoPreference: Preference? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		addPreferencesFromResource(R.xml.preferences)
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		initialization()
		monitor()
		return super.onCreateView(inflater, container, savedInstanceState)
	}

	private fun initialization() {
		resultNumberPreference = findPreference(getString(R.string.key_result_number))
		similarityPreference = findPreference(getString(R.string.key_similarity))
		licensePreference = findPreference(getString(R.string.key_license))
		janyoInfoPreference = findPreference(getString(R.string.key_info_janyo))

		resultNumberPreference!!.setSummary(R.string.summary_result_number)
	}

	private fun monitor() {
		resultNumberPreference!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
			val view = LayoutInflater.from(activity).inflate(R.layout.dialog_edit, TextInputLayout(activity), false)
			val textInputLayout = view.findViewById<TextInputLayout>(R.id.layout)
			textInputLayout.hint = getString(R.string.title_result_number)
			textInputLayout.editText!!.inputType = InputType.TYPE_CLASS_NUMBER
			textInputLayout.editText!!.setText(Settings.resultNumber.toString())
			AlertDialog.Builder(activity)
					.setTitle(" ")
					.setView(view)
					.setPositiveButton(android.R.string.ok) { dialogInterface, i -> Settings.resultNumber = Integer.parseInt(textInputLayout.editText!!.text.toString()) }
					.setNegativeButton(android.R.string.cancel, null)
					.show()
			false
		}
		similarityPreference!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
			val view = LayoutInflater.from(activity).inflate(R.layout.dialog_edit, TextInputLayout(activity), false)
			val textInputLayout = view.findViewById<TextInputLayout>(R.id.layout)
			textInputLayout.hint = getString(R.string.title_similarity)
			textInputLayout.editText!!.inputType = InputType.TYPE_CLASS_NUMBER
			textInputLayout.editText!!.setText((Settings.similarity * 100).toString())
			AlertDialog.Builder(activity)
					.setTitle(" ")
					.setView(view)
					.setPositiveButton(android.R.string.ok) { dialogInterface, i ->
						val similarity = java.lang.Float.parseFloat(textInputLayout.editText!!.text.toString())
						Settings.similarity = similarity / 100f
					}
					.setNegativeButton(android.R.string.cancel, null)
					.show()
			false
		}
		licensePreference!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
			val view_license = LayoutInflater.from(activity).inflate(R.layout.dialog_license, NestedScrollView(activity), false)
			val text_license_point1 = view_license.findViewById<TextView>(R.id.license_point1)
			val text_license_point2 = view_license.findViewById<TextView>(R.id.license_point2)
			val text_license_point3 = view_license.findViewById<TextView>(R.id.license_point3)
			val point = VectorDrawableCompat.create(resources, R.drawable.ic_point, null)
			point!!.setBounds(0, 0, point.minimumWidth, point.minimumHeight)
			text_license_point1.setCompoundDrawables(point, null, null, null)
			text_license_point2.setCompoundDrawables(point, null, null, null)
			text_license_point3.setCompoundDrawables(point, null, null, null)
			AlertDialog.Builder(activity)
					.setTitle(" ")
					.setView(view_license)
					.setPositiveButton(android.R.string.ok, null)
					.show()
			false
		}
		janyoInfoPreference!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
			val view_info = LayoutInflater.from(activity).inflate(R.layout.dialog_info, NestedScrollView(activity), false)
			AlertDialog.Builder(activity)
					.setTitle(" ")
					.setView(view_info)
					.setPositiveButton(android.R.string.ok, null)
					.show()
			false
		}
	}
}
