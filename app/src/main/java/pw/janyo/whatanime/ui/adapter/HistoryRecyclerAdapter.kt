package pw.janyo.whatanime.ui.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import pw.janyo.whatanime.R
import pw.janyo.whatanime.databinding.ActivityHistoryBinding
import pw.janyo.whatanime.databinding.ItemHistoryBinding
import pw.janyo.whatanime.factory.GsonFactory
import pw.janyo.whatanime.handler.HistoryItemListener
import pw.janyo.whatanime.model.Animation
import pw.janyo.whatanime.model.AnimationHistory
import vip.mystery0.tools.base.BaseRecyclerViewAdapter
import java.text.SimpleDateFormat
import java.util.*

class HistoryRecyclerAdapter(private val context: Context,
							 private val activityHistoryBinding: ActivityHistoryBinding,
							 list: ArrayList<in AnimationHistory>) : BaseRecyclerViewAdapter<HistoryRecyclerAdapter.ViewHolder, AnimationHistory>(context, R.layout.item_history, list) {
	private val options = RequestOptions()
			.diskCacheStrategy(DiskCacheStrategy.NONE)

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(createView(parent))

	override fun setItemView(holder: ViewHolder, position: Int, data: AnimationHistory) {
		val animation = GsonFactory.gson.fromJson<Animation>(data.result, Animation::class.java)
		holder.binding.handler = HistoryItemListener(context, activityHistoryBinding)
		holder.binding.animation = animation
		holder.binding.history = data
		Glide.with(context).load(data.cachePath).apply(options).into(holder.binding.imageView)
		if (animation.docs.isNotEmpty()) {
			holder.binding.animationDocs = animation.docs[0]
			val dateFormat = SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss", Locale.CHINA)
			val calendar = Calendar.getInstance()
			calendar.timeInMillis = data.time
			holder.binding.textViewTime.text = dateFormat.format(calendar.time)
			val similarity = "${animation.docs[0].similarity * 100}%"
			holder.binding.textViewSimilarity.text = similarity
		} else {
			holder.binding.textViewSimilarity.text = "0%"
		}
	}

	class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		val binding = DataBindingUtil.bind<ItemHistoryBinding>(itemView)!!
	}
}