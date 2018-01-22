package pw.janyo.whatanime.util;

import android.content.Context;
import android.content.SharedPreferences;

import pw.janyo.whatanime.APP;

public class Settings {
    private static final SharedPreferences sharedPreference = APP.getContext().getSharedPreferences("settings", Context.MODE_PRIVATE);

    public static void setResultNumber(int value) {
        sharedPreference.edit().putInt("resultNumber", value).apply();
    }

    public static int getResultNumber() {
        return sharedPreference.getInt("resultNumber", 1);
    }

    public static void setFirst() {
        sharedPreference.edit().putBoolean("isFirst", false).apply();
    }

    public static boolean isFirst() {
        return sharedPreference.getBoolean("isFirst", true);
    }

    public static void setSimilarity(float value) {
        sharedPreference.edit().putFloat("similarity", value).apply();
    }

    public static float getSimilarity() {
        return sharedPreference.getFloat("similarity", 0f);
    }

    public static void setFilter(String value) {
        sharedPreference.edit().putString("filter", value).apply();
    }

    public static String getFilter() {
        return sharedPreference.getString("filter", null);
    }
}
