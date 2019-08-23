package pw.janyo.whatanime.ui.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import pw.janyo.whatanime.R
import pw.janyo.whatanime.constant.Constant
import pw.janyo.whatanime.databinding.ItemSearchResultBinding
import pw.janyo.whatanime.handler.MainItemListener
import pw.janyo.whatanime.model.Docs
import vip.mystery0.tools.base.binding.BaseBindingRecyclerViewAdapter
import vip.mystery0.tools.utils.formatTime
import vip.mystery0.tools.utils.toCalendar
import vip.mystery0.tools.utils.toTimeString
import java.net.URLEncoder
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class MainRecyclerAdapter(private val context: Context,
						  private val listener: MainItemListener) : BaseBindingRecyclerViewAdapter<Docs, ItemSearchResultBinding>(R.layout.item_search_result) {
	override fun setItemView(binding: ItemSearchResultBinding, position: Int, data: Docs) {
		binding.handler = listener
		binding.animationDocs = data
		val requestUrl = "${Constant.baseUrl}thumbnail.php?anilist_id=" + data.anilist_id + "&file=" + URLEncoder.encode(data.filename, "UTF-8") + "&t=" + data.at + "&token=" + data.tokenthumb
		Glide.with(context).load(requestUrl).placeholder(R.mipmap.janyo_studio)
				.error(R.drawable.ic_load_failed).into(binding.imageView)
		val nativeTitle = "${context.getString(R.string.hint_title_native)}${data.title_native}"
		val chineseTitle = "${context.getString(R.string.hint_title_chinese)}${data.title_chinese}"
		val englishTitle = "${context.getString(R.string.hint_title_english)}${data.title_english}"
		val romajiTitle = "${context.getString(R.string.hint_title_romaji)}${data.title_romaji}"
		binding.textViewTitleNative.text = nativeTitle
		binding.textViewTitleChinese.text = chineseTitle
		binding.textViewTitleEnglish.text = englishTitle
		binding.textViewTitleRomaji.text = romajiTitle
		val dateFormat = SimpleDateFormat("mm:ss", Locale.CHINA)
		val calendar = Calendar.getInstance()
		calendar.timeInMillis = data.at.toLong() * 1000
		binding.textViewTime.text = dateFormat.format(calendar.time)
		binding.textViewAniListId.text = data.anilist_id.toString()
		binding.textViewMalId.text = data.mal_id.toString()
		val similarity = "${DecimalFormat("#.000").format(data.similarity * 100)}%"
		binding.textViewSimilarity.text = similarity
		binding.cardView.setOnClickListener {
			MaterialAlertDialogBuilder(context)
					.setTitle(" ")
					.setMessage(R.string.hint_show_animation_detail)
					.setPositiveButton(android.R.string.ok) { _, _ ->
						val intent = Intent(Intent.ACTION_VIEW)
						intent.data = Uri.parse("https://anilist.co/anime/${data.anilist_id}")
						if (intent.resolveActivity(context.packageManager) != null) {
							context.startActivity(Intent.createChooser(intent, context.getString(R.string.hint_select_browser)))
						} else {
							Toast.makeText(context, R.string.hint_no_browser, Toast.LENGTH_LONG)
									.show()
						}
					}
					.setNegativeButton(android.R.string.cancel, null)
					.show()
		}
	}
}