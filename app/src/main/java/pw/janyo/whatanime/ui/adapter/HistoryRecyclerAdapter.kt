package pw.janyo.whatanime.ui.adapter

import android.content.Context
import coil.api.load
import coil.request.CachePolicy
import pw.janyo.whatanime.R
import pw.janyo.whatanime.databinding.ItemHistoryBinding
import pw.janyo.whatanime.handler.HistoryItemListener
import pw.janyo.whatanime.model.Animation
import pw.janyo.whatanime.model.AnimationHistory
import vip.mystery0.tools.base.binding.BaseBindingRecyclerViewAdapter
import vip.mystery0.tools.factory.fromJson
import vip.mystery0.tools.utils.getCalendarFromLong
import vip.mystery0.tools.utils.toDateTimeString
import java.io.File
import java.text.DecimalFormat

class HistoryRecyclerAdapter(context: Context,
							 private val listener: HistoryItemListener) : BaseBindingRecyclerViewAdapter<AnimationHistory, ItemHistoryBinding>(R.layout.item_history) {

	override fun setItemView(binding: ItemHistoryBinding, position: Int, data: AnimationHistory) {
		val animation = data.result.fromJson<Animation>()
		binding.handler = listener
		binding.animation = animation
		binding.history = data
		binding.imageView.load(File(data.cachePath)) {
			diskCachePolicy(CachePolicy.DISABLED)
		}
		if (animation.docs.isNotEmpty()) {
			binding.animationDocs = animation.docs[0]
			binding.textViewTime.text = data.time.getCalendarFromLong().toDateTimeString()
			val similarity = "${DecimalFormat("#.0000").format(animation.docs[0].similarity * 100)}%"
			binding.textViewSimilarity.text = similarity
		} else {
			binding.textViewSimilarity.text = "0%"
		}
	}
}