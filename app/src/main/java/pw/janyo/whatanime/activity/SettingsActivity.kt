package pw.janyo.whatanime.activity

import android.os.Bundle
import android.preference.PreferenceActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.appcompat.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout

import pw.janyo.whatanime.R
import pw.janyo.whatanime.fragment.SettingsFragment

/**
 * Created by myste.
 */

class SettingsActivity : PreferenceActivity() {
	private var toolbar: Toolbar? = null
	lateinit var coordinatorLayout: CoordinatorLayout

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		fragmentManager.beginTransaction().replace(R.id.content_wrapper, SettingsFragment()).commit()
		toolbar!!.title = title
	}

	override fun setContentView(layoutResID: Int) {
		val contentView = LayoutInflater.from(this).inflate(R.layout.activity_settings, LinearLayout(this), false) as ViewGroup
		toolbar = contentView.findViewById(R.id.toolbar)
		toolbar!!.setNavigationOnClickListener { finish() }
		coordinatorLayout = contentView.findViewById(R.id.coordinatorLayout)

		val contentWrapper = contentView.findViewById<ViewGroup>(R.id.content_wrapper)
		LayoutInflater.from(this).inflate(layoutResID, contentWrapper, true)

		window.setContentView(contentView)
	}
}
