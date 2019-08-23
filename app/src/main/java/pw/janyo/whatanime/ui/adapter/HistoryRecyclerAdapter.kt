package pw.janyo.whatanime.ui.adapter

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import pw.janyo.whatanime.R
import pw.janyo.whatanime.databinding.ItemHistoryBinding
import pw.janyo.whatanime.handler.HistoryItemListener
import pw.janyo.whatanime.model.Animation
import pw.janyo.whatanime.model.AnimationHistory
import vip.mystery0.tools.base.binding.BaseBindingRecyclerViewAdapter
import vip.mystery0.tools.factory.fromJson
import vip.mystery0.tools.utils.toCalendar
import vip.mystery0.tools.utils.toDateTimeString
import java.text.DecimalFormat

class HistoryRecyclerAdapter(private val context: Context,
							 private val listener: HistoryItemListener) : BaseBindingRecyclerViewAdapter<AnimationHistory, ItemHistoryBinding>(R.layout.item_history) {

	private val options = RequestOptions()
			.diskCacheStrategy(DiskCacheStrategy.NONE)

	override fun setItemView(binding: ItemHistoryBinding, position: Int, data: AnimationHistory) {
		val animation = data.result.fromJson<Animation>()
		binding.handler = listener
		binding.animation = animation
		binding.history = data
		Glide.with(context).load(data.cachePath).apply(options).into(binding.imageView)
		if (animation.docs.isNotEmpty()) {
			binding.animationDocs = animation.docs[0]
			binding.textViewTime.text = data.time.toCalendar().toDateTimeString()
			val similarity = "${DecimalFormat("#.0000").format(animation.docs[0].similarity * 100)}%"
			binding.textViewSimilarity.text = similarity
		} else {
			binding.textViewSimilarity.text = "0%"
		}
	}
}