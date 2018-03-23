package pw.janyo.whatanime;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import org.litepal.LitePal;

import vip.mystery0.logs.Logs;

/**
 * Created by mystery0.
 */

public class APP extends Application {
    @SuppressLint("StaticFieldLeak")
    private static Context context;

    public static Context getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
//		Logs.setLevel(Logs.Level.RELEASE);
        LitePal.initialize(this);
    }
}
