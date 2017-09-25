package pw.janyo.whatanime.util.whatanime;

import android.content.Context;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import pw.janyo.whatanime.classes.Animation;
import pw.janyo.whatanime.classes.Dock;
import pw.janyo.whatanime.classes.History;
import pw.janyo.whatanime.listener.WhatAnimeBuildListener;
import pw.janyo.whatanime.util.Settings;
import pw.janyo.whatanime.util.WAFileUti;
import vip.mystery0.tools.HTTPok.HTTPok;
import vip.mystery0.tools.HTTPok.HTTPokException;
import vip.mystery0.tools.HTTPok.HTTPokResponse;
import vip.mystery0.tools.HTTPok.HTTPokResponseListener;
import vip.mystery0.tools.Logs.Logs;

/**
 * Created by myste.
 */

public class WhatAnimeBuilder
{
	private static final String TAG = "WhatAnimeBuilder";
	private WhatAnime whatAnime;
	private OkHttpClient mOkHttpClient;
	private History history;

	public WhatAnimeBuilder()
	{
		whatAnime = new WhatAnime();
		mOkHttpClient = new OkHttpClient.Builder()
				.connectTimeout(10, TimeUnit.SECONDS)
				.readTimeout(20, TimeUnit.SECONDS)
				.build();
		history = new History();
	}

	public void setImgFile(String path)
	{
		whatAnime.setPath(path);
		history.setImaPath(path);
	}

	public void build(final Context context, String url, final WhatAnimeBuildListener listener)
	{
		String base64 = whatAnime.base64Data(whatAnime.compressBitmap(whatAnime.getBitmapFromFile()));
		Map<String, String> map = new HashMap<>();
		map.put("image", base64);
		new HTTPok()
				.setURL(url)
				.setRequestMethod(HTTPok.Companion.getPOST())
				.setParams(map)
				.setOkHttpClient(mOkHttpClient)
				.setListener(new HTTPokResponseListener()
				{
					@Override
					public void onError(String msg)
					{
						listener.error(new HTTPokException(msg));
					}

					@Override
					public void onResponse(HTTPokResponse httPokResponse)
					{
						String md5;
						try
						{
							MessageDigest md = MessageDigest.getInstance("MD5");
							String date = Calendar.getInstance().getTime().toString();
							md.update(date.getBytes());
							md5 = new BigInteger(1, md.digest()).toString(16);
							Logs.i(TAG, "onResponse: " + md5);
							File jsonFile = new File(context.getCacheDir() + File.separator + "json" + File.separator + md5);
							jsonFile.getParentFile().mkdirs();
							Logs.i(TAG, "onResponse: " + httPokResponse.getFile(jsonFile));
							FileReader fileReader = new FileReader(jsonFile);
							Animation animation = new Gson().fromJson(fileReader, Animation.class);
							String cacheImgPath = context.getCacheDir() + File.separator + "img" + File.separator + md5;
							WAFileUti.fileCopy(history.getImaPath(), cacheImgPath);
							history.setCachePath(cacheImgPath);
							history.setTitle(animation.docs.get(0).title);
							history.setSaveFilePath(jsonFile.getAbsolutePath());
							history.saveOrUpdate("imaPath = ?", history.getImaPath());
							listener.done(animation);
						} catch (FileNotFoundException | NoSuchAlgorithmException e)
						{
							listener.error(e);
						}
					}
				})
				.open();
	}
}
