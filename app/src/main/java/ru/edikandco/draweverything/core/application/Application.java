package ru.edikandco.draweverything.core.application;

import android.content.Context;


import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import java.io.File;

import httpimage.BitmapMemoryCache;
import httpimage.FileSystemPersistence;
import httpimage.HttpImageManager;
import ru.edikandco.draweverything.R;
import ru.edikandco.draweverything.core.cache.CacheDirectoryManagerImpl;
import ru.edikandco.draweverything.core.http.ExtendedHttpClient;
import ru.edikandco.draweverything.core.http.HttpBitmapReader;
import ru.edikandco.draweverything.core.http.HttpBytesReader;
import ru.edikandco.draweverything.core.http.HttpStreamReader;
import ru.edikandco.draweverything.core.service.API;
import ru.edikandco.draweverything.core.service.LocalImageManager;
import ru.edikandco.draweverything.core.util.ServiceContainer;

/**
 * Created by Semyon Danilov on 02.08.2014.
 */
@ReportsCrashes(formKey = "", // will not be used
        mailTo = "senya.danilov@gmail.com",
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.crash_toast_text)
public class Application extends android.app.Application {

    private static Context context;

    @Override
    public void onCreate() {
        context = getApplicationContext();
        File mydir = getBaseContext().getDir("mydir", Context.MODE_PRIVATE);
        CacheDirectoryManagerImpl cacheDirectoryManager = new CacheDirectoryManagerImpl(mydir, ApplicationSettings.get(this), ApplicationSettings.PACKAGE_NAME);
        FileSystemPersistence fsp = new FileSystemPersistence(cacheDirectoryManager);
        HttpStreamReader httpStreamReader = new HttpStreamReader(new ExtendedHttpClient(), getResources());
        HttpBytesReader httpBytesReader = new HttpBytesReader(httpStreamReader, getResources());
        HttpBitmapReader httpBitmapReader = new HttpBitmapReader(httpBytesReader);
        BitmapMemoryCache bmc = new BitmapMemoryCache(0.4f);
        HttpImageManager httpImageManager = new HttpImageManager(bmc, fsp, getResources(), httpBitmapReader);
        LocalImageManager localImageManager = new LocalImageManager(bmc, getResources());
        ServiceContainer.addService(cacheDirectoryManager);
        ServiceContainer.addService(new API());
        ServiceContainer.addService(httpBytesReader);
        ServiceContainer.addService(httpStreamReader);
        ServiceContainer.addService(httpImageManager);
        ServiceContainer.addService(localImageManager);


        //Google Play Service ты офигел!
        try {
            Class.forName("android.os.AsyncTask");
        } catch(Throwable ignore) {
        }

        // The following line triggers the initialization of ACRA
        ACRA.init(this);
    }

    public static Context getContext() {
        return context;
    }

}
