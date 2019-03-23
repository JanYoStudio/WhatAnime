package pw.janyo.whatanime.ui.adapter

import android.content.Context
import com.bumptech.glide.Glide
import pw.janyo.whatanime.R
import pw.janyo.whatanime.constant.Constant
import pw.janyo.whatanime.databinding.ItemSearchResultBinding
import pw.janyo.whatanime.handler.MainItemListener
import pw.janyo.whatanime.model.Docs
import vip.mystery0.tools.base.binding.BaseBindingRecyclerViewAdapter
import java.net.URLEncoder
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
		val dateFormat = SimpleDateFormat("mm:ss", Locale.CHINA)
		val calendar = Calendar.getInstance()
		calendar.timeInMillis = data.at.toLong() * 1000
		binding.textViewTime.text = dateFormat.format(calendar.time)
		binding.textViewAniListId.text = data.anilist_id.toString()
		binding.textViewMalId.text = data.mal_id.toString()
		val similarity = "${data.similarity * 100}%"
		binding.textViewSimilarity.text = similarity
	}
}