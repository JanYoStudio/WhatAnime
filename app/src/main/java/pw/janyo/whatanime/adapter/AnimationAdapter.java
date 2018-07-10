package pw.janyo.whatanime.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import pw.janyo.whatanime.R;
import pw.janyo.whatanime.classes.Dock;
import pw.janyo.whatanime.util.TextViewUtils;

public class AnimationAdapter extends RecyclerView.Adapter<AnimationAdapter.ViewHolder> {
	private Context context;
	private List<Dock> list;
	private OnClickListener onClickListener;

	public AnimationAdapter(Context context, List<Dock> list) {
		this.context = context;
		this.list = list;
	}

	public void setOnClickListener(OnClickListener onClickListener) {
		this.onClickListener = onClickListener;
	}

	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_animation, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int position) {
		final Dock dock = list.get(position);
		StringBuilder hintStringBuilder = new StringBuilder();
		hintStringBuilder.append("标题：").append('\n')
				.append("中文标题：").append('\n')
				.append("时间：").append('\n')
				.append("准确度：").append('\n')
				.append("episode：").append('\n')
				.append("anilist_id：").append('\n')
				.append("mal_id：").append('\n')
				.append("日语标题：").append('\n')
				.append("英文标题：").append('\n')
				.append("罗马字：");
		StringBuilder stringBuilder = new StringBuilder();
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss", Locale.CHINA);
		stringBuilder.append(dock.title).append('\n');
		stringBuilder.append(dock.title_chinese).append('\n');
		calendar.setTimeInMillis((long) dock.at * 1000);
		stringBuilder.append(dateFormat.format(calendar.getTime())).append('\n');
		stringBuilder.append(dock.similarity * 100).append('%').append('\n');
		stringBuilder.append(dock.episode).append('\n');
		stringBuilder.append(dock.anilist_id).append('\n');
		stringBuilder.append(dock.mal_id).append('\n');
		stringBuilder.append(dock.title_native).append('\n');
		stringBuilder.append(dock.title_english).append('\n');
		stringBuilder.append(dock.title_romaji);
		viewHolder.hintTextView.setText(hintStringBuilder.toString());
		viewHolder.textView.setText(stringBuilder.toString());
		try {
			String requestUrl = "https://whatanime.ga/thumbnail.php?season=" + dock.season + "&anime=" + URLEncoder.encode(dock.anime, "UTF-8") + "&file=" + URLEncoder.encode(dock.filename, "UTF-8") + "&t=" + dock.at + "&token=" + dock.tokenthumb;
			Glide.with(context).load(requestUrl).into(viewHolder.imageView);
		} catch (Exception e) {
			e.printStackTrace();
		}
		TextViewUtils.setMaxLinesWithAnimation(viewHolder.hintTextView, 4);
		TextViewUtils.setMaxLinesWithAnimation(viewHolder.textView, 4);
		viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				TextViewUtils.setMaxLinesWithAnimation(viewHolder.hintTextView, viewHolder.hintTextView.getMaxLines() == Integer.MAX_VALUE ? 4 : Integer.MAX_VALUE);
				TextViewUtils.setMaxLinesWithAnimation(viewHolder.textView, viewHolder.textView.getMaxLines() == Integer.MAX_VALUE ? 4 : Integer.MAX_VALUE);
			}
		});
		viewHolder.imageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (onClickListener != null)
					onClickListener.onClick(dock);
			}
		});
	}

	@Override
	public int getItemCount() {
		return list.size();
	}

	class ViewHolder extends RecyclerView.ViewHolder {
		ImageView imageView;
		TextView hintTextView;
		TextView textView;

		ViewHolder(View itemView) {
			super(itemView);
			imageView = itemView.findViewById(R.id.imageView);
			hintTextView = itemView.findViewById(R.id.textView_hint);
			textView = itemView.findViewById(R.id.textView);
		}
	}

	public interface OnClickListener {
		void onClick(Dock dock);
	}
}
