package pw.janyo.whatanime.ui.fragment

import android.os.Bundle
import androidx.preference.PreferenceFragment
import pw.janyo.whatanime.R

class AboutFragment : PreferenceFragment() {
	override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
		setPreferencesFromResource(R.xml.pref_about, rootKey)
	}
}