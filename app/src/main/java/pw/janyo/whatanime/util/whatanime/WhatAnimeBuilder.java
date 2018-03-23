package pw.janyo.whatanime.util.whatanime;

import android.content.Context;
import android.os.Environment;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;

import com.google.gson.Gson;
import com.zyao89.view.zloading.ZLoadingDialog;
import com.zyao89.view.zloading.Z_TYPE;

import java.io.File;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import pw.janyo.whatanime.R;
import pw.janyo.whatanime.activity.MainActivity;
import pw.janyo.whatanime.adapter.AnimationAdapter;
import pw.janyo.whatanime.classes.Animation;
import pw.janyo.whatanime.classes.Dock;
import pw.janyo.whatanime.classes.History;
import pw.janyo.whatanime.interfaces.SearchService;
import pw.janyo.whatanime.util.Base64;
import pw.janyo.whatanime.util.Base64DecoderException;
import pw.janyo.whatanime.util.Settings;
import pw.janyo.whatanime.util.WAFileUtil;
import retrofit2.HttpException;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import vip.mystery0.logs.Logs;

public class WhatAnimeBuilder {
	private static final String TAG = "WhatAnimeBuilder";
    private CoordinatorLayout coordinatorLayout;
    private String token;
    private WhatAnime whatAnime;
    private Retrofit retrofit;
    private ZLoadingDialog zLoadingDialog;
    private History history;

    public WhatAnimeBuilder(Context context) {
        whatAnime = new WhatAnime();
        coordinatorLayout = ((MainActivity) context).findViewById(R.id.coordinatorLayout);
        try {
            token = new String(Base64.decode(context.getString(R.string.token)));
        } catch (Base64DecoderException e) {
            e.printStackTrace();
        }
        OkHttpClient mOkHttpClient = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build();
        retrofit = new Retrofit.Builder()
                .baseUrl(context.getString(R.string.requestUrl))
                .client(mOkHttpClient)
				.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
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
            public void subscribe(final ObservableEmitter<Animation> subscriber) {
                String base64 = whatAnime.base64Data(whatAnime.compressBitmap());
                retrofit.create(SearchService.class)
						.search(token,base64,null)
						.subscribeOn(Schedulers.newThread())
						.unsubscribeOn(Schedulers.newThread())
						.map(new Function<ResponseBody, Animation>() {
							@Override
							public Animation apply(ResponseBody responseBody) {
								return new Gson().fromJson(new InputStreamReader(responseBody.byteStream()),Animation.class);
							}
						})
						.observeOn(Schedulers.newThread())
						.subscribe(new Observer<Animation>() {
							private Animation animation;
							@Override
							public void onSubscribe(Disposable d) {

							}

							@Override
							public void onNext(Animation animation) {
								this.animation=animation;
							}

							@Override
							public void onError(Throwable e) {
								Logs.wtf(TAG, "onError: ", e);
								subscriber.onComplete();
							}

							@Override
							public void onComplete() {subscriber.onNext(animation);
								try {
									MessageDigest messageDigest = MessageDigest.getInstance("MD5");
									messageDigest.update(Calendar.getInstance().getTime().toString().getBytes());
									String md5 = new BigInteger(1, messageDigest.digest()).toString(16);
									File jsonFile = new File(context.getExternalFilesDir(null) + File.separator + "json" + File.separator + md5);
									WAFileUtil.saveJson(animation, jsonFile);
									String cacheImgPath = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator + md5;
									WAFileUtil.fileCopy(history.getImaPath(), cacheImgPath);
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
								} catch (NoSuchAlgorithmException e) {
									subscriber.onError(e);
								}
							}
						});

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
                        if (e instanceof HttpException) {
                            HttpException httpException = (HttpException) e;
                            String message = httpException.response().message();
                            int code = httpException.response().code();
                            if (code == 429) {
                                Snackbar.make(coordinatorLayout, R.string.hint_http_exception_busy, Snackbar.LENGTH_LONG)
                                        .show();
                            } else
                                Snackbar.make(coordinatorLayout, context.getString(R.string.hint_http_exception_error, code, message), Snackbar.LENGTH_LONG)
                                        .show();
                        }
                        Snackbar.make(coordinatorLayout, context.getString(R.string.hint_other_error, e.getMessage()), Snackbar.LENGTH_LONG)
                                .show();
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        zLoadingDialog.dismiss();
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}
