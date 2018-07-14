package pw.janyo.whatanime.ui

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import pw.janyo.whatanime.R
import pw.janyo.whatanime.databinding.ItemSearchResultBinding
import pw.janyo.whatanime.model.Docs
import vip.mystery0.tools.base.BaseRecyclerViewAdapter
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

class MainRecyclerAdapter(private val context: Context,
						  list: ArrayList<in Docs>) : BaseRecyclerViewAdapter<MainRecyclerAdapter.ViewHolder, Docs>(context, R.layout.item_search_result, list) {
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		return ViewHolder(createView(parent))
	}

	override fun setItemView(holder: ViewHolder, position: Int, data: Docs) {
		holder.binding.animationDocs = data
		val requestUrl = "https://whatanime.ga/thumbnail.php?anilist_id=" + data.anilist_id + "&file=" + URLEncoder.encode(data.filename, "UTF-8") + "&t=" + data.at + "&token=" + data.tokenthumb
		Glide.with(context).load(requestUrl).into(holder.binding.imageView)
		val dateFormat = SimpleDateFormat("mm:ss", Locale.CHINA)
		val calendar = Calendar.getInstance()
		calendar.timeInMillis = data.at.toLong() * 1000
		holder.binding.textViewTime.text = dateFormat.format(calendar.time)
		holder.binding.textViewAniListId.text = data.anilist_id.toString()
		holder.binding.textViewMalId.text = data.mal_id.toString()
		val similarity = "${data.similarity * 100}%"
		holder.binding.textViewSimilarity.text = similarity
	}

	class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		val binding = DataBindingUtil.bind<ItemSearchResultBinding>(itemView)!!
	}
}