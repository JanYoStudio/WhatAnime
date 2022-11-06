package pw.janyo.whatanime.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import pw.janyo.whatanime.R

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        supportFragmentManager.beginTransaction()
//            .replace(R.id.content_wrapper, AboutFragment())
//            .commit()
        title = getString(R.string.title_activity_settings)
//        val toolbar = findViewById<Toolbar>(R.id.toolbar)
//        toolbar.title = title
//        toolbar.setNavigationOnClickListener {
//            finish()
//        }
    }
}