package pw.janyo.whatanime.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import org.litepal.crud.DataSupport;

import java.util.List;

import pw.janyo.whatanime.R;
import pw.janyo.whatanime.adapter.HistoryAdapter;
import pw.janyo.whatanime.classes.History;
import pw.janyo.whatanime.util.WAFileUtil;

public class HistoryActivity extends AppCompatActivity {
	private Toolbar toolbar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initialization();
		monitor();
	}

	private void initialization() {
		setContentView(R.layout.activity_history);
		toolbar = findViewById(R.id.toolbar);

		final List<History> list = WAFileUtil.checkList(DataSupport.findAll(History.class));
		RecyclerView recyclerView = findViewById(R.id.recyclerView);
		recyclerView.setLayoutManager(new LinearLayoutManager(HistoryActivity.this));
		final HistoryAdapter adapter = new HistoryAdapter(HistoryActivity.this, list);
		ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
			@Override
			public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
				return false;
			}

			@Override
			public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
				WAFileUtil.deleteHistory(list.get(viewHolder.getAdapterPosition()));
				list.remove(viewHolder.getAdapterPosition());
				adapter.notifyItemRemoved(viewHolder.getAdapterPosition());
			}
		};
		new ItemTouchHelper(callback).attachToRecyclerView(recyclerView);
		recyclerView.setAdapter(adapter);

		setSupportActionBar(toolbar);
	}

	private void monitor() {
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				finish();
			}
		});
	}
}
