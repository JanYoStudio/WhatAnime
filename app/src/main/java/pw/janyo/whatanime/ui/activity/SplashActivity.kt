package pw.janyo.whatanime.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.ViewDataBinding
import pw.janyo.whatanime.base.WABaseActivity
import pw.janyo.whatanime.config.Configure

class SplashActivity : WABaseActivity<ViewDataBinding>(null) {
	override fun initView() {
		super.initView()
		when (Configure.nightMode) {
			0 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
			1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
			2 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
			3 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
		}
	}

	override fun initData() {
		super.initData()
		startActivity(Intent(this, MainActivity::class.java))
		finish()
	}
}
