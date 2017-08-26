package pw.janyo.whatanime;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.support.design.widget.FloatingActionButton;

import com.mystery0.tools.FileUtil.FileUtil;
import com.mystery0.tools.Logs.Logs;

public class MainActivity extends AppCompatActivity
{
	private static final String TAG = "MainActivity";
	private final static int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 233;
	private final static int REQUEST_CODE = 322;
	private AnalysisHandler analysisHandler = new AnalysisHandler();
	private ProgressDialog progressDialog;
	private AnimationAdapter adapter;
	private String baseURL = "https://whatanime.ga/api/search?token=2b85c7881b18fe81062387e979144f62c85788c9";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestPermission();
		setContentView(R.layout.activity_main);

		FloatingActionButton main_fab_upload = findViewById(R.id.main_fab_upload);
		Toolbar toolbar = findViewById(R.id.toolbar);
		RecyclerView recyclerView = findViewById(R.id.recyclerView);
		recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
		analysisHandler.list = new ArrayList<>();
		adapter = new AnimationAdapter(MainActivity.this, analysisHandler.list);
		recyclerView.setAdapter(adapter);
		analysisHandler.adapter = adapter;
		analysisHandler.context = MainActivity.this;

		setSupportActionBar(toolbar);
		//noinspection ConstantConditions
		getSupportActionBar().setDisplayShowTitleEnabled(true);

		progressDialog = new ProgressDialog(MainActivity.this);
		progressDialog.setMessage("搜索中……");
		progressDialog.setCancelable(false);
		analysisHandler.progressDialog = progressDialog;

		main_fab_upload.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View p1)
			{
				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(intent, REQUEST_CODE);
			}
		});
	}

	private void requestPermission()
	{
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
				!= PackageManager.PERMISSION_GRANTED)
		{
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
					WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
		}
	}

	private void Search(String base64)
	{
		OkHttpClient mOkHttpClient = new OkHttpClient.Builder().build();
		RequestBody mRequestBody = new FormBody.Builder()
				.add("image", base64)
				.build();
		Request mRequest = new Request.Builder()
				.url(baseURL)
				.post(mRequestBody)
				.build();
		Call call = mOkHttpClient.newCall(mRequest);
		call.enqueue(new Callback()
		{
			@Override
			public void onFailure(Call p1, IOException p2)
			{
			}

			@Override
			public void onResponse(Call p1, Response p2) throws IOException
			{
				Logs.i(TAG, ": 132");
				Message message = new Message();
				message.obj = p2.body().string();
				message.what = 0;
				analysisHandler.sendMessage(message);
			}
		});
	}

	private String encodeToBase64(String path)
	{
		String base64 = "";
		try
		{
			File file = new File(path);
			FileInputStream inputFile = new FileInputStream(file);
			byte[] buffer = new byte[(int) file.length()];
			inputFile.read(buffer);
			inputFile.close();
			base64 = Base64.encodeToString(buffer, Base64.NO_WRAP);

			//tv_base64.setText(encodeString);
//			Log.d("BitmapToBase64", base64);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return base64;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
	{
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == WRITE_EXTERNAL_STORAGE_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED)
		{
			Log.i(TAG, "onRequestPermissionsResult: 获得权限");
		} else
		{
			finish();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		if (REQUEST_CODE == requestCode && RESULT_OK == resultCode)
		{
			final Uri uri = data.getData();
			Log.i(TAG, "onActivityResult: " + uri);

			progressDialog.show();
			try
			{
				String path = FileUtil.getPath(MainActivity.this, uri);
				Logs.i(TAG, ": " + path);
				adapter.setImgPath(path);
				String base64 = encodeToBase64(path);
				Search(base64);
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}
