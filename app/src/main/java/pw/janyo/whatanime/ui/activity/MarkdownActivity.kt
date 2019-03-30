package pw.janyo.whatanime.ui.activity

import android.content.Context
import android.content.Intent
import pw.janyo.whatanime.R

import pw.janyo.whatanime.base.WABaseActivity
import pw.janyo.whatanime.databinding.ActivityMarkdownBinding
import pw.janyo.whatanime.utils.FileUtil
import ru.noties.markwon.Markwon

class MarkdownActivity : WABaseActivity<ActivityMarkdownBinding>(R.layout.activity_markdown) {
	private var fileName: String = ""

	companion object {
		fun intentTo(context: Context, fileName: String) {
			val intent = Intent(context, MarkdownActivity::class.java)
			intent.putExtra("fileName", fileName)
			context.startActivity(intent)
		}
	}

	override fun initView() {
		super.initView()
		setSupportActionBar(binding.toolbar)
		supportActionBar!!.setDisplayHomeAsUpEnabled(true)
		binding.toolbar.setNavigationOnClickListener {
			finish()
		}
	}

	override fun initData() {
		super.initData()
		if (intent != null && intent.hasExtra("fileName"))
			fileName = intent.getStringExtra("fileName")
		else
			finish()
		when (fileName) {
			"about.md" -> title = getString(R.string.action_about)
			"faq.md" -> title = getString(R.string.action_faq)
		}
		binding.toolbar.title = title
	}

	override fun loadDataToView() {
		super.loadDataToView()
		if (fileName == "") {
			finish()
			return
		}
		Markwon.create(this)
				.setMarkdown(binding.textView, FileUtil.getMarkdown(this, fileName))
	}
}
