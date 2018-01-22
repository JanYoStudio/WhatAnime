package pw.janyo.whatanime.util.whatanime;

import android.content.Context;
import android.os.Environment;
import android.support.v4.content.ContextCompat;

import com.zyao89.view.zloading.ZLoadingDialog;
import com.zyao89.view.zloading.Z_TYPE;

import java.io.File;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import pw.janyo.whatanime.R;
import pw.janyo.whatanime.adapter.AnimationAdapter;
import pw.janyo.whatanime.classes.Animation;
import pw.janyo.whatanime.classes.Dock;
import pw.janyo.whatanime.classes.History;
import pw.janyo.whatanime.interfaces.SearchService;
import pw.janyo.whatanime.util.Base64;
import pw.janyo.whatanime.util.Base64DecoderException;
import pw.janyo.whatanime.util.Settings;
import pw.janyo.whatanime.util.WAFileUti;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import vip.mystery0.tools.logs.Logs;

/**
 * Created by myste.
 */

public class WhatAnimeBuilder {
    private static final String TAG = "WhatAnimeBuilder";
    private String token;
    private WhatAnime whatAnime;
    private Retrofit retrofit;
    private ZLoadingDialog zLoadingDialog;
    private History history;

    public WhatAnimeBuilder(Context context) {
        whatAnime = new WhatAnime();
        try {
            token = new String(Base64.decode(context.getString(R.string.token)));
        } catch (Base64DecoderException e) {
            e.printStackTrace();
        }
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient mOkHttpClient = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build();
        retrofit = new Retrofit.Builder()
                .baseUrl(context.getString(R.string.requestUrl))
                .client(mOkHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        zLoadingDialog = new ZLoadingDialog(context)
                .setLoadingBuilder(Z_TYPE.STAR_LOADING)
                .setHintText("正在搜索……")
                .setHintTextSize(16F)
                .setCanceledOnTouchOutside(false)
                .setLoadingColor(ContextCompat.getColor(context, R.color.colorAccent))
                .setHintTextColor(ContextCompat.getColor(context, R.color.colorAccent));
        history = new History();
    }

    public void setImgFile(String path) {
        whatAnime.setPath(path);
        history.setImaPath(path);
    }

    public void build(final Context context, final List<Dock> list, final AnimationAdapter adapter) {
        Observable.create(new ObservableOnSubscribe<Animation>() {
            @Override
            public void subscribe(ObservableEmitter<Animation> subscriber) throws Exception {
                String base64 = whatAnime.base64Data(whatAnime.compressBitmap(whatAnime.getBitmapFromFile()));
                Animation animation = retrofit.create(SearchService.class)
                        .search(token, base64, null)
                        .execute()
                        .body();
                if (animation == null) {
                    Logs.i(TAG, "subscribe: 返回的数据解析为空");
                    subscriber.onComplete();
                    return;
                }
                subscriber.onNext(animation);
                MessageDigest messageDigest = MessageDigest.getInstance("MD5");
                messageDigest.update(Calendar.getInstance().getTime().toString().getBytes());
                String md5 = new BigInteger(1, messageDigest.digest()).toString(16);
                File jsonFile = new File(context.getExternalFilesDir(null) + File.separator + "json" + File.separator + md5);
                WAFileUti.saveJson(animation, jsonFile);
                String cacheImgPath = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator + md5;
                WAFileUti.fileCopy(history.getImaPath(), cacheImgPath);
                history.setCachePath(cacheImgPath);
                history.setTitle(animation.docs.get(0).title);
                history.setSaveFilePath(jsonFile.getAbsolutePath());
                history.saveOrUpdate("imaPath = ?", history.getImaPath());
                list.clear();
                if (Settings.getResultNumber() < list.size()) {
                    list.addAll(animation.docs.subList(0, Settings.getResultNumber()));
                } else {
                    list.addAll(animation.docs);
                }
                if (Settings.getSimilarity() != 0f) {
                    Iterator<Dock> iterator = list.iterator();
                    while (iterator.hasNext()) {
                        Dock dock = iterator.next();
                        if (dock.similarity < Settings.getSimilarity())
                            iterator.remove();
                    }
                }
                subscriber.onComplete();
            }
        })
                .subscribeOn(Schedulers.newThread())
                .unsubscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Animation>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        zLoadingDialog.show();
                    }

                    @Override
                    public void onNext(Animation animation) {
                    }

                    @Override
                    public void onError(Throwable e) {
                        zLoadingDialog.dismiss();
                        Logs.wtf(TAG, "onError: ", e);
                    }

                    @Override
                    public void onComplete() {
                        zLoadingDialog.dismiss();
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}
