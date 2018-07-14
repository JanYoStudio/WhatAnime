package pw.janyo.whatanime.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import pw.janyo.whatanime.R
import pw.janyo.whatanime.databinding.ActivityAboutBinding
import pw.janyo.whatanime.ui.fragment.AboutFragment

class AboutActivity : AppCompatPreferenceActivity() {
	private lateinit var activityAboutBinding: ActivityAboutBinding
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		fragmentManager.beginTransaction().replace(R.id.content_wrapper, AboutFragment()).commit()
		activityAboutBinding.toolbar.title = title
		activityAboutBinding.toolbar.setNavigationOnClickListener {
			finish()
		}
	}

	override fun setContentView(layoutResID: Int) {
		activityAboutBinding = ActivityAboutBinding.inflate(LayoutInflater.from(this))
		window.setContentView(activityAboutBinding.root)
	}
}