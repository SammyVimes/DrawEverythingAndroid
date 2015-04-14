package ru.edikandco.draweverything.core.util;

import android.os.Environment;

import java.io.File;

/**
 * Created by Semyon Danilov on 16.05.2014.
 */
public class Constants {

    public static final long FILE_CACHE_THRESHOLD = IoUtils.convertMbToBytes(25);
    public static final long FILE_CACHE_TRIM_AMOUNT = IoUtils.convertMbToBytes(15);
    public static final String USER_AGENT_STRING = "";
    public static final String SDPATH = Environment.getExternalStorageDirectory().toString() + "/android/ru.edikandco.draweverything/downloads/";
    {
        new File(SDPATH).mkdirs();
    }

}
