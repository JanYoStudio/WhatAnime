package pw.janyo.whatanime.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import org.litepal.crud.DataSupport;

import java.util.List;

import pw.janyo.whatanime.R;
import pw.janyo.whatanime.adapter.HistoryAdapter;
import pw.janyo.whatanime.classes.History;
import pw.janyo.whatanime.util.WAFileUti;

public class HistoryActivity extends AppCompatActivity
{
	private Toolbar toolbar;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		initialization();
		monitor();
	}

	private void initialization()
	{
		setContentView(R.layout.activity_history);
		toolbar = findViewById(R.id.toolbar);

		List<History> list = WAFileUti.checkList(HistoryActivity.this, DataSupport.findAll(History.class));
		RecyclerView recyclerView = findViewById(R.id.recyclerView);
		recyclerView.setLayoutManager(new LinearLayoutManager(HistoryActivity.this));
		recyclerView.setAdapter(new HistoryAdapter(HistoryActivity.this, list));

		setSupportActionBar(toolbar);
	}

	private void monitor()
	{
		toolbar.setNavigationOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				finish();
			}
		});
	}
}
