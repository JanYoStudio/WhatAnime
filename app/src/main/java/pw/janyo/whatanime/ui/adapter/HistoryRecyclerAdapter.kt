package pw.janyo.whatanime.ui.adapter

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import pw.janyo.whatanime.R
import pw.janyo.whatanime.databinding.ItemHistoryBinding
import pw.janyo.whatanime.handler.HistoryItemListener
import pw.janyo.whatanime.model.Animation
import pw.janyo.whatanime.model.AnimationHistory
import pw.janyo.whatanime.utils.loadWithoutCache
import vip.mystery0.tools.base.binding.BaseBindingRecyclerViewAdapter
import vip.mystery0.tools.factory.fromJson
import vip.mystery0.tools.utils.getCalendarFromLong
import vip.mystery0.tools.utils.toDateTimeString
import java.io.File
import java.text.DecimalFormat

class HistoryRecyclerAdapter :
    BaseBindingRecyclerViewAdapter<AnimationHistory, ItemHistoryBinding>(R.layout.item_history),
    KoinComponent {
    private val listener: HistoryItemListener by inject()

    override fun setItemView(binding: ItemHistoryBinding, position: Int, data: AnimationHistory) {
        val animation = data.result.fromJson<Animation>()
        binding.handler = listener
        binding.animation = animation
        binding.history = data
        binding.imageView.loadWithoutCache(File(data.cachePath))
        if (animation.docs.isNotEmpty()) {
            binding.animationDocs = animation.docs[0]
            binding.textViewTime.text = data.time.getCalendarFromLong().toDateTimeString()
            val similarity =
                "${DecimalFormat("#.0000").format(animation.docs[0].similarity * 100)}%"
            binding.textViewSimilarity.text = similarity
        } else {
            binding.textViewSimilarity.text = "0%"
        }
    }
}