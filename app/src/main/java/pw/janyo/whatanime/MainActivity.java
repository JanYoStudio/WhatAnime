package pw.janyo.whatanime;

import android.Manifest;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import android.support.design.widget.FloatingActionButton;

public class MainActivity extends AppCompatActivity
{
	private static final String TAG = "MainActivity";
	private final static int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 233;
	private final static int REQUEST_CODE = 322;
	private FloatingActionButton main_fab_upload;
	private TextView tv_text;
	private ImageView image;
	private ProgressBar mProgressBar;
	private Toolbar main_toolbar;
	private String baseURL = "https://whatanime.ga/api/search?token=2b85c7881b18fe81062387e979144f62c85788c9";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestPermission();
		setContentView(R.layout.main);

		main_fab_upload = (FloatingActionButton) findViewById(R.id.main_fab_upload);
		tv_text = (TextView) findViewById(R.id.TextView);
		image = (ImageView) findViewById(R.id.ImageView);
		mProgressBar = (ProgressBar) findViewById(R.id.ProgressBar);
		main_toolbar = (Toolbar) findViewById(R.id.main_toolbar);

		setSupportActionBar(main_toolbar);
		//noinspection ConstantConditions
		getSupportActionBar().setDisplayShowTitleEnabled(true);

		main_fab_upload.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View p1)
			{
				// TODO: Implement this method
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
			//申请WRITE_EXTERNAL_STORAGE权限
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
				// TODO: Implement this method
				final String response = p2.body().string();
				runOnUiThread(new Runnable()
				{

					@Override
					public void run()
					{
						try
						{
							JSONArray mJSONArray = new JSONObject(response).getJSONArray("docs");
							for (int i = 0; i < mJSONArray.length(); i++)
							{
								JSONObject mJSONObject = (JSONObject) mJSONArray.get(i);
								String name = mJSONObject.getString("title");
								String chineseName = mJSONObject.getJSONArray("synonyms_chinese").get(i).toString();
								String episode = mJSONObject.getString("episode");
								String from = mJSONObject.getString("from");
								Float time_ms_float = Float.parseFloat(from) * 1000;
								int time_ms_int = time_ms_float.intValue();
								Date time_ms = new Date(time_ms_int);
								int time_m = time_ms.getMinutes();
								int time_s = time_ms.getSeconds();

								tv_text.setText("番名:" + name + "\n中文翻译:" + chineseName + "\n集数:" + episode + "\n时间:" + time_m + ":" + time_s);
								mProgressBar.setVisibility(View.GONE);
							}
						} catch (Exception e)
						{
							e.printStackTrace();
						}
					}


				});
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
			Log.d("BitmapToBase64", base64);
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
			Uri uri = data.getData();
			Log.i(TAG, "onActivityResult: " + uri);
			try
			{
				String[] strings = {MediaStore.Images.Media.DATA};
				CursorLoader mCursorLoader = new CursorLoader(this, uri, strings, null, null, null);
				Cursor mCursor = mCursorLoader.loadInBackground();
				int mInt = mCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				mCursor.moveToFirst();
				String path = mCursor.getString(mInt);
				//Toast.makeText(getApplicationContext(),path,Toast.LENGTH_SHORT).show();
				Bitmap bitmap = BitmapFactory.decodeFile(path);
				image.setImageBitmap(bitmap);
				String base64 = encodeToBase64(path);
				mProgressBar.setVisibility(View.VISIBLE);
				Search(base64);
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}
