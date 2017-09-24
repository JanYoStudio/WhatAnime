package pw.janyo.whatanime.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import pw.janyo.whatanime.R;
import pw.janyo.whatanime.classes.History;

/**
 * Created by myste.
 */

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder>
{
	private Context context;
	private List<History> list;

	public HistoryAdapter(Context context, List<History> list)
	{
		this.context = context;
		this.list = list;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position)
	{
		History history = list.get(position);
		Glide.with(context).load(history.getImaPath()).into(holder.imageView);
		holder.text_title.setText(history.getTitle());
	}

	@Override
	public int getItemCount()
	{
		return list.size();
	}

	class ViewHolder extends RecyclerView.ViewHolder
	{
		ImageView imageView;
		TextView text_hint;
		TextView text_title;

		ViewHolder(View itemView)
		{
			super(itemView);
			imageView = itemView.findViewById(R.id.image);
			text_hint = itemView.findViewById(R.id.text_hint);
			text_title = itemView.findViewById(R.id.text_title);
		}
	}
}
