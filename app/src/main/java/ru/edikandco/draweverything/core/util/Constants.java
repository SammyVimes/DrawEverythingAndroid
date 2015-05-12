package ru.edikandco.draweverything.core.util;

import android.os.Environment;

import java.io.File;
import java.io.IOException;

/**
 * Created by Semyon Danilov on 16.05.2014.
 */
public class Constants {

    public static final long FILE_CACHE_THRESHOLD = IoUtils.convertMbToBytes(25);
    public static final long FILE_CACHE_TRIM_AMOUNT = IoUtils.convertMbToBytes(15);
    public static final String USER_AGENT_STRING = "";
    public static final String SDPATH = Environment.getExternalStorageDirectory().toString() + "/android/data/ru.edikandco.draweverything/downloads/";
    static {
        File f = new File(SDPATH);
        f.mkdirs();
        File noMedia = new File(f.getPath() + "/.nomedia");
        try {
            noMedia.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
