package pw.janyo.whatanime.ui.adapter

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import pw.janyo.whatanime.R
import pw.janyo.whatanime.databinding.ItemHistoryBinding
import pw.janyo.whatanime.factory.GsonFactory
import pw.janyo.whatanime.handler.HistoryItemListener
import pw.janyo.whatanime.model.Animation
import pw.janyo.whatanime.model.AnimationHistory
import vip.mystery0.tools.base.binding.BaseBindingRecyclerViewAdapter
import java.text.SimpleDateFormat
import java.util.*

class HistoryRecyclerAdapter(private val context: Context,
							 private val listener: HistoryItemListener) : BaseBindingRecyclerViewAdapter<AnimationHistory, ItemHistoryBinding>(R.layout.item_history) {

	private val options = RequestOptions()
			.diskCacheStrategy(DiskCacheStrategy.NONE)

	override fun setItemView(binding: ItemHistoryBinding, position: Int, data: AnimationHistory) {
		val animation = GsonFactory.gson.fromJson<Animation>(data.result, Animation::class.java)
		binding.handler = listener
		binding.animation = animation
		binding.history = data
		Glide.with(context).load(data.cachePath).apply(options).into(binding.imageView)
		if (animation.docs.isNotEmpty()) {
			binding.animationDocs = animation.docs[0]
			val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
			val calendar = Calendar.getInstance()
			calendar.timeInMillis = data.time
			binding.textViewTime.text = dateFormat.format(calendar.time)
			val similarity = "${animation.docs[0].similarity * 100}%"
			binding.textViewSimilarity.text = similarity
		} else {
			binding.textViewSimilarity.text = "0%"
		}
	}
}