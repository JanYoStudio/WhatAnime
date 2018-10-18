package pw.janyo.whatanime.ui.activity

import android.os.Bundle
import pw.janyo.whatanime.R
import pw.janyo.whatanime.databinding.ActivityAboutBinding
import pw.janyo.whatanime.ui.fragment.AboutFragment
import vip.mystery0.tools.base.binding.BaseBindingActivity

class AboutActivity : BaseBindingActivity<ActivityAboutBinding>(R.layout.activity_about) {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		supportFragmentManager.beginTransaction().replace(R.id.content_wrapper, AboutFragment()).commit()
		binding.toolbar.title = title
		binding.toolbar.setNavigationOnClickListener {
			finish()
		}
	}
}