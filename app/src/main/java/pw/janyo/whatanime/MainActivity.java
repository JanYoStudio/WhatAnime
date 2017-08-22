package pw.janyo.whatanime;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.mystery0.tools.FileUtil.FileUtil;
import com.mystery0.tools.Logs.Logs;
import com.mystery0.tools.MysteryNetFrameWork.HttpUtil;
import com.mystery0.tools.MysteryNetFrameWork.ResponseListener;

public class MainActivity extends AppCompatActivity
{
	private static final String TAG = "MainActivity";
	private final static int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 233;
	private final static int REQUEST_CODE = 322;
	private RequestQueue requestQueue;
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
		setContentView(R.layout.activity_main);

		requestQueue = Volley.newRequestQueue(MainActivity.this);

		main_fab_upload = findViewById(R.id.main_fab_upload);
		tv_text = findViewById(R.id.TextView);
		image = findViewById(R.id.ImageView);
		mProgressBar = findViewById(R.id.ProgressBar);
		main_toolbar = findViewById(R.id.toolbar);

		setSupportActionBar(main_toolbar);
		//noinspection ConstantConditions
		getSupportActionBar().setDisplayShowTitleEnabled(true);

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
			//申请WRITE_EXTERNAL_STORAGE权限
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
					WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
		}
	}

	private void Search(String base64)
	{
//		OkHttpClient mOkHttpClient = new OkHttpClient.Builder().build();
//		RequestBody mRequestBody = new FormBody.Builder()
//				.add("image", base64)
//				.build();
//		Request mRequest = new Request.Builder()
//				.url(baseURL)
//				.post(mRequestBody)
//				.build();
//		Call call = mOkHttpClient.newCall(mRequest);
//		call.enqueue(new Callback()
//		{
//			@Override
//			public void onFailure(Call p1, IOException p2)
//			{
//			}
//
//			@Override
//			public void onResponse(Call p1, Response p2) throws IOException
//			{
//				final String response = p2.body().string();
//				Logs.i(TAG, ": " + response);
//				runOnUiThread(new Runnable()
//				{
//					@Override
//					public void run()
//					{
//						try
//						{
//							JSONArray mJSONArray = new JSONObject(response).getJSONArray("docs");
//							for (int i = 0; i < mJSONArray.length(); i++)
//							{
//								JSONObject mJSONObject = (JSONObject) mJSONArray.get(i);
//								String name = mJSONObject.getString("title");
//								String chineseName = mJSONObject.getJSONArray("synonyms_chinese").get(i).toString();
//								String episode = mJSONObject.getString("episode");
//								String from = mJSONObject.getString("from");
//								Float time_ms_float = Float.parseFloat(from) * 1000;
//								int time_ms_int = time_ms_float.intValue();
//								Calendar calendar=Calendar.getInstance();
//								calendar.setTimeInMillis(time_ms_int);
////								Date time_ms = new Date(time_ms_int);
////								int time_s = time_ms.getSeconds();
////								int time_m = time_ms.getMinutes();
//								int time_s=calendar.get(Calendar.SECOND);
//								int time_m=calendar.get(Calendar.MINUTE);
//
//								tv_text.setText("番名:" + name + "\n中文翻译:" + chineseName + "\n集数:" + episode + "\n时间:" + time_m + ":" + time_s);
//								mProgressBar.setVisibility(View.GONE);
//							}
//						} catch (Exception e)
//						{
//							e.printStackTrace();
//						}
//					}
//				});
//			}
//		});

		Map<String, String> map = new HashMap<>();
		map.put("image", base64);
		new HttpUtil(MainActivity.this)
				.setRequestQueue(requestQueue)
				.setRequestMethod(HttpUtil.RequestMethod.POST)
				.setUrl(baseURL)
				.setMap(map)
				.setResponseListener(new ResponseListener()
				{
					@Override
					public void onResponse(int i, String s)
					{
						Logs.i(TAG, ": " + s);
						try
						{
							Animation animation = new Gson().fromJson(s, Animation.class);
							Logs.i(TAG, ": " + animation.docs.size());
							Logs.i(TAG, ": " + animation.docs.get(0).title);
							Logs.i(TAG, ": " + animation.docs.get(0).synonyms_chinese.get(0));
							Logs.i(TAG, ": " + animation.docs.get(0).episode);
							Logs.i(TAG, ": " + animation.docs.get(0).from);
							Calendar calendar = Calendar.getInstance();
							calendar.setTimeInMillis(((int) animation.docs.get(0).from));

						} catch (Exception e)
						{
							e.printStackTrace();
						}
					}
				})
				.open();
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
//				String[] strings = {MediaStore.Images.Media.DATA};
//				CursorLoader mCursorLoader = new CursorLoader(this, uri, strings, null, null, null);
//				Cursor mCursor = mCursorLoader.loadInBackground();
//				int mInt = mCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
//				mCursor.moveToFirst();
//				String path = mCursor.getString(mInt);
				//Toast.makeText(getApplicationContext(),path,Toast.LENGTH_SHORT).show();
				String path = FileUtil.getPath(MainActivity.this, uri);
				Logs.i(TAG, ": " + path);
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
