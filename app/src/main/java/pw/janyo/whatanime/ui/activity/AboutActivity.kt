package pw.janyo.whatanime.ui.activity

import android.os.Bundle
import pw.janyo.whatanime.R
import pw.janyo.whatanime.base.WABaseActivity
import pw.janyo.whatanime.databinding.ActivityAboutBinding
import pw.janyo.whatanime.ui.fragment.AboutFragment

class AboutActivity : WABaseActivity<ActivityAboutBinding>(R.layout.activity_about) {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		supportFragmentManager.beginTransaction().replace(R.id.content_wrapper, AboutFragment())
				.commit()
		title = getString(R.string.title_activity_settings)
		binding.toolbar.title = title
		binding.toolbar.setNavigationOnClickListener {
			finish()
		}
	}
}