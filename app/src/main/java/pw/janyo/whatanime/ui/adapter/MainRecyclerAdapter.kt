package pw.janyo.whatanime.ui.adapter

import android.content.Context
import coil.api.load
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import pw.janyo.whatanime.R
import pw.janyo.whatanime.config.toBrowser
import pw.janyo.whatanime.constant.Constant
import pw.janyo.whatanime.databinding.ItemSearchResultBinding
import pw.janyo.whatanime.handler.MainItemListener
import pw.janyo.whatanime.model.Docs
import vip.mystery0.tools.base.binding.BaseBindingRecyclerViewAdapter
import vip.mystery0.tools.utils.formatTime
import java.net.URLEncoder
import java.text.DecimalFormat

class MainRecyclerAdapter(private val context: Context,
						  private val listener: MainItemListener) : BaseBindingRecyclerViewAdapter<Docs, ItemSearchResultBinding>(R.layout.item_search_result) {
	override fun setItemView(binding: ItemSearchResultBinding, position: Int, data: Docs) {
		binding.handler = listener
		binding.animationDocs = data
		val requestUrl = "${Constant.baseUrl}thumbnail.php?anilist_id=" + data.anilist_id + "&file=" + URLEncoder.encode(data.filename, "UTF-8") + "&t=" + data.at + "&token=" + data.tokenthumb
		binding.imageView.load(requestUrl) {
			placeholder(R.mipmap.janyo_studio)
			error(R.drawable.ic_load_failed)
		}
		val nativeTitle = "${context.getString(R.string.hint_title_native)}${data.title_native}"
		val chineseTitle = "${context.getString(R.string.hint_title_chinese)}${data.title_chinese}"
		val englishTitle = "${context.getString(R.string.hint_title_english)}${data.title_english}"
		val romajiTitle = "${context.getString(R.string.hint_title_romaji)}${data.title_romaji}"
		binding.textViewTitleNative.text = nativeTitle
		binding.textViewTitleChinese.text = chineseTitle
		binding.textViewTitleEnglish.text = englishTitle
		binding.textViewTitleRomaji.text = romajiTitle
		binding.textViewTime.text = (data.at.toLong() * 1000).formatTime()
		binding.textViewAniListId.text = data.anilist_id.toString()
		binding.textViewMalId.text = data.mal_id.toString()
		val similarity = "${DecimalFormat("#.000").format(data.similarity * 100)}%"
		binding.textViewSimilarity.text = similarity
		binding.cardView.setOnClickListener {
			MaterialAlertDialogBuilder(context)
					.setTitle(context.getString(R.string.hint_show_animation_detail, data.title_native))
					.setPositiveButton(android.R.string.ok) { _, _ ->
						context.toBrowser("https://anilist.co/anime/${data.anilist_id}")
					}
					.setNegativeButton(android.R.string.cancel, null)
					.show()
		}
	}
}