package pw.janyo.whatanime.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import com.bumptech.glide.Glide

import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

import pw.janyo.whatanime.R
import pw.janyo.whatanime.classes.Dock
import pw.janyo.whatanime.util.TextViewUtils

class AnimationAdapter(private val context: Context, private val list: List<Dock>) : RecyclerView.Adapter<AnimationAdapter.ViewHolder>() {
	private var onClickListener: OnClickListener? = null

	fun setOnClickListener(onClickListener: OnClickListener) {
		this.onClickListener = onClickListener
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val view = LayoutInflater.from(parent.context).inflate(R.layout.item_animation, parent, false)
		return ViewHolder(view)
	}

	override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
		val dock = list[position]
		val hintStringBuilder = StringBuilder()
		hintStringBuilder.append("标题：").append('\n')
				.append("中文标题：").append('\n')
				.append("时间：").append('\n')
				.append("准确度：").append('\n')
				.append("episode：").append('\n')
				.append("anilist_id：").append('\n')
				.append("mal_id：").append('\n')
				.append("日语标题：").append('\n')
				.append("英文标题：").append('\n')
				.append("罗马字：")
		val stringBuilder = StringBuilder()
		val calendar = Calendar.getInstance()
		val dateFormat = SimpleDateFormat("mm:ss", Locale.CHINA)
		stringBuilder.append(dock.title).append('\n')
		stringBuilder.append(dock.title_chinese).append('\n')
		calendar.timeInMillis = dock.at.toLong() * 1000
		stringBuilder.append(dateFormat.format(calendar.time)).append('\n')
		stringBuilder.append(dock.similarity * 100).append('%').append('\n')
		stringBuilder.append(dock.episode).append('\n')
		stringBuilder.append(dock.anilist_id).append('\n')
		stringBuilder.append(dock.mal_id).append('\n')
		stringBuilder.append(dock.title_native).append('\n')
		stringBuilder.append(dock.title_english).append('\n')
		stringBuilder.append(dock.title_romaji)
		viewHolder.hintTextView.text = hintStringBuilder.toString()
		viewHolder.textView.text = stringBuilder.toString()
		try {
			val requestUrl = "https://whatanime.ga/thumbnail.php?season=" + dock.season + "&anime=" + URLEncoder.encode(dock.anime, "UTF-8") + "&file=" + URLEncoder.encode(dock.filename, "UTF-8") + "&t=" + dock.at + "&token=" + dock.tokenthumb
			Glide.with(context).load(requestUrl).into(viewHolder.imageView)
		} catch (e: Exception) {
			e.printStackTrace()
		}

		TextViewUtils.setMaxLinesWithAnimation(viewHolder.hintTextView, 4)
		TextViewUtils.setMaxLinesWithAnimation(viewHolder.textView, 4)
		viewHolder.itemView.setOnClickListener {
			TextViewUtils.setMaxLinesWithAnimation(viewHolder.hintTextView, if (viewHolder.hintTextView.maxLines == Integer.MAX_VALUE) 4 else Integer.MAX_VALUE)
			TextViewUtils.setMaxLinesWithAnimation(viewHolder.textView, if (viewHolder.textView.maxLines == Integer.MAX_VALUE) 4 else Integer.MAX_VALUE)
		}
		viewHolder.imageView.setOnClickListener {
			if (onClickListener != null)
				onClickListener!!.onClick(dock)
		}
	}

	override fun getItemCount(): Int {
		return list.size
	}

	 inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		var imageView: ImageView
		var hintTextView: TextView
		var textView: TextView

		init {
			imageView = itemView.findViewById(R.id.imageView)
			hintTextView = itemView.findViewById(R.id.textView_hint)
			textView = itemView.findViewById(R.id.textView)
		}
	}

	interface OnClickListener {
		fun onClick(dock: Dock)
	}
}
