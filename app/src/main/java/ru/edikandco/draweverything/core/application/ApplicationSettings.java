package ru.edikandco.draweverything.core.application;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * Created by Semyon Danilov on 16.05.2014.
 */
public class ApplicationSettings {

    private static final String TAG = "ApplicationSettings";
    public static final String PACKAGE_NAME = "com.danilov.mangareaderplus";

    private static final String DOWNLOAD_PATH_FIELD = "DPF";
    private static final String MANGA_DOWNLOAD_BASE_PATH_FIELD = "MDBPF";
    private static final String TUTORIAL_VIEWER_PASSED_FIELD = "TVPF";
    private static final String VIEWER_FULLSCREEN_FIELD = "VFF";
    private static final String FIRST_LAUNCH = "FL";
    private static final String TUTORIAL_MENU_PASSED_FIELD = "TMP";
    private static final String SHOW_VIEWER_BTNS_ALWAYS_FIELD = "SVBA";
    private static final String MAIN_MENU_ITEM_FIELD = "MMI";

    private static ApplicationSettings instance;

    private String downloadPath;

    public String getDownloadPath() {
        return downloadPath;
    }

    public void setDownloadPath(final String downloadPath) {
        this.downloadPath = downloadPath;
    }


    public static ApplicationSettings get(final Context context) {
        if (instance == null) {
            instance = new ApplicationSettings(context);
        }
        return instance;
    }

    private ApplicationSettings(final Context context) {
        load(context);
    }

    private void load(final Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        this.downloadPath = sharedPreferences.getString(DOWNLOAD_PATH_FIELD, "");

    }

    public void update(final Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(DOWNLOAD_PATH_FIELD, downloadPath);
        editor.commit();
    }

}
