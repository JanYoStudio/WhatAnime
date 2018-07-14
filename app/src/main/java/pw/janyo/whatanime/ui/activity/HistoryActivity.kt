package pw.janyo.whatanime.ui.activity

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;
import pw.janyo.whatanime.R

import kotlinx.android.synthetic.main.activity_history.*

class HistoryActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_history)
		setSupportActionBar(toolbar)
	}

}
