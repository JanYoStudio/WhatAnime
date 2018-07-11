//package pw.janyo.whatanime.adapter
//
//import android.content.Context
//import android.content.Intent
//import android.os.Bundle
//import androidx.recyclerview.widget.RecyclerView
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import android.widget.TextView
//
//import com.bumptech.glide.Glide
//
//import pw.janyo.whatanime.R
//import pw.janyo.whatanime.activity.DetailActivity
//import pw.janyo.whatanime.classes.History
//
///**
// * Created by myste.
// */
//
//class HistoryAdapter(private val context: Context, private val list: List<History>) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {
//
//	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//		val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
//		return ViewHolder(view)
//	}
//
//	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//		val history = list[position]
//		Glide.with(context).load(history.cachePath).into(holder.imageView)
//		holder.text_title.text = history.title
//		holder.itemView.setOnClickListener {
//			val intent = Intent(context, DetailActivity::class.java)
//			val bundle = Bundle()
//			bundle.putSerializable("history", history)
//			intent.putExtra("history", bundle)
//			context.startActivity(intent)
//		}
//	}
//
//	override fun getItemCount(): Int {
//		return list.size
//	}
//
//	inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//		var imageView: ImageView
//		var text_hint: TextView
//		var text_title: TextView
//
//		init {
//			imageView = itemView.findViewById(R.id.image)
//			text_hint = itemView.findViewById(R.id.text_hint)
//			text_title = itemView.findViewById(R.id.text_title)
//		}
//	}
//}
