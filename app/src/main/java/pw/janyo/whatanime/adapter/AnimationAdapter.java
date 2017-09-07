package pw.janyo.whatanime.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.Calendar;
import java.util.List;

import pw.janyo.whatanime.R;
import pw.janyo.whatanime.classes.Dock;

public class AnimationAdapter extends RecyclerView.Adapter<AnimationAdapter.ViewHolder>
{
	private Context context;
	private List<Dock> list;
	private String imgPath;

	public AnimationAdapter(Context context, List<Dock> list)
	{
		this.context = context;
		this.list = list;
	}

	public void setImgPath(String imgPath)
	{
		this.imgPath = imgPath;
		notifyDataSetChanged();
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_animation, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position)
	{
		if (position == 0)
		{
			holder.text_name.setVisibility(View.GONE);
			holder.text_chinese_name.setVisibility(View.GONE);
			holder.text_number.setVisibility(View.GONE);
			holder.text_time.setVisibility(View.GONE);
			Glide.with(context).load(imgPath).into(holder.imageView);
		} else
		{
			Dock dock = list.get(position - 1);
			holder.text_name.setText(context.getString(R.string.text_name, dock.title));
			StringBuilder temp = new StringBuilder();
			for (String t : dock.synonyms_chinese)
			{
				temp.append(t).append("，");
			}
			holder.text_chinese_name.setText(context.getString(R.string.text_chinese_name, temp.toString()));
			holder.text_number.setText(context.getString(R.string.text_number, dock.episode));
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(((long) dock.at));
			int time_s = calendar.get(Calendar.SECOND);
			int time_m = calendar.get(Calendar.MINUTE);
			holder.text_time.setText(context.getString(R.string.text_time, time_m, time_s));
		}
	}

	@Override
	public int getItemCount()
	{
		return list.size() + 1;
	}

	class ViewHolder extends RecyclerView.ViewHolder
	{
		ImageView imageView;
		TextView text_name;
		TextView text_chinese_name;
		TextView text_number;
		TextView text_time;

		ViewHolder(View itemView)
		{
			super(itemView);
			imageView = itemView.findViewById(R.id.imageView);
			text_name = itemView.findViewById(R.id.text_name);
			text_chinese_name = itemView.findViewById(R.id.text_chinese_name);
			text_number = itemView.findViewById(R.id.text_number);
			text_time = itemView.findViewById(R.id.text_time);
		}
	}
}
