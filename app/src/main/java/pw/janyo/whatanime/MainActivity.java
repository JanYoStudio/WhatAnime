package pw.janyo.whatanime;

import android.app.Activity;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;
import org.json.JSONException;
import org.json.JSONArray;
import org.json.JSONTokener;
import android.widget.ImageView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ProgressBar;
import android.transition.Visibility;

public class MainActivity extends Activity 
{
	private int REQUEST_CODE;
	private Button btn_upload;
	private TextView tv_text;
	private ImageView image;
	private ProgressBar mProgressBar;
	private String baseURL = "";
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		
		btn_upload = (Button) findViewById(R.id.upload);
		tv_text = (TextView) findViewById(R.id.TextView);
		image = (ImageView) findViewById(R.id.ImageView);
		mProgressBar = (ProgressBar) findViewById(R.id.ProgressBar);
		
		btn_upload.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View p1)
				{
					// TODO: Implement this method
					Intent intent = new Intent();
					intent.setType("image/*");
					intent.setAction(intent.ACTION_GET_CONTENT);
					startActivityForResult(intent,REQUEST_CODE);
				}
		});
    }
	
	private void Search(String base64)
	{
		OkHttpClient mOkHttpClient = new OkHttpClient.Builder().build();
		RequestBody mRequestBody = new FormBody.Builder()
			.add("image",base64)
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
					// TODO: Implement this method
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
									for(int i = 0;i < mJSONArray.length();i++)
									{
										JSONObject mJSONObject = (JSONObject)mJSONArray.get(i);
										String name = mJSONObject.getString("title");
										String chinesename = mJSONObject.getJSONArray("synonyms_chinese").get(i).toString();
										String episode = mJSONObject.getString("episode");
										String from = mJSONObject.getString("from");
										tv_text.setText("番名:"+name+"\n中文翻译:"+chinesename+"\n集数:"+episode+"\n时间:"+from);
										mProgressBar.setVisibility(View.GONE);
									}
								}
								catch(Exception e)
								{
									e.printStackTrace();
								}
								// TODO: Implement this method
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
			// TODO: Implement this method
			File file = new File(path);
			FileInputStream inputFile = new FileInputStream(file);
			byte[] buffer = new byte[(int)file.length()];
			inputFile.read(buffer);
			inputFile.close();
			base64 = Base64.encodeToString(buffer,Base64.NO_WRAP);

			//tv_base64.setText(encodeString);
			Log.d("BitmapToBase64",base64);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return base64;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode,resultCode,data);
		if(REQUEST_CODE == requestCode && RESULT_OK == resultCode)
		{
			Uri uri = data.getData();
			try
			{
				String [] strings = {MediaStore.Images.Media.DATA};
				CursorLoader mCursorLoader = new CursorLoader(this,uri,strings,null,null,null);
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
			}
			catch(Exception e)
			{
				
			}
		}
	}
}
