package io.anyline.adcubum.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PrefsUtil {

    private static final String SUBJECT_COUNT = "SUBJECT_COUNT";
    private static final String SUBJECT = "SUBJECT";

    private static SharedPreferences getSharedPrefs(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static int getSubjectCount(Context context){
        return getSharedPrefs(context).getInt(SUBJECT_COUNT, 1);
    }

    public static void incrSubjectCount(Context context) {
        int currentCount = getSubjectCount(context);
        getSharedPrefs(context)
                .edit()
                .putInt(SUBJECT_COUNT, currentCount + 1)
                .apply();
    }

    public static String getSubject(Context context) {
        return getSharedPrefs(context).getString(SUBJECT, "");
    }

    public static void clearSubject(Context context) {
        getSharedPrefs(context).edit().putString(SUBJECT, "").apply();
    }
}
