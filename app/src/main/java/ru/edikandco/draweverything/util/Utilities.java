package ru.edikandco.draweverything.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;



/**
 * Created by Эдуард on 23.06.2014.
 */
public class Utilities {
    public static boolean isNetworkConnected(Activity activity) {
        ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {
            return false;
        } else
            return true;
    }

    private static Handler mHandler;
    private static Locale curLocale;
    private static String color;

    public static void getHtmlNews(Handler _mHandler, Locale _curLocale, String _color) {
        mHandler = _mHandler;
        curLocale = _curLocale;
        color = _color;

        new WorkingThread().start();
    }


    static class WorkingThread extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                HttpClient httpclient = new DefaultHttpClient(); // Create HTTP Client
                HttpGet httpget = new HttpGet("http://draw.kvins.ru/android_news.php?l=" + curLocale.toString().substring(0, 2)); // Set the action you want to do
                HttpResponse response = httpclient.execute(httpget); // Executeit
                HttpEntity entity = response.getEntity();
                InputStream is = entity.getContent(); // Create an InputStream with the response
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) // Read line by line
                    sb.append(line + "\n");
                is.close(); // Close the stream

                Message m = new Message();
                m.what = 0;

                m.obj = sb.toString().replaceFirst("CODECOLORCODE", color);
                mHandler.sendMessage(m);

            } catch (Exception ex) {
                System.out.println("e.getMessage(): " + ex.getMessage());
                System.out.println("e.toString(): " + ex);
                System.out.println("e.printStackTrace():");
                ex.printStackTrace(System.out);

                Message m = new Message();
                m.what = 0;
                m.obj = "-1";
                mHandler.sendMessage(m);
            }
        }
    }


    public static String convertRGBToHex(int color) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = (color >> 0) & 0xFF;

        String rFString, rSString, gFString, gSString, bFString, bSString, result;
        int red, green, blue;
        int rred, rgreen, rblue;

        red = r / 16;
        rred = r % 16;

        if (red == 10) rFString = "A";
        else if (red == 11) rFString = "B";
        else if (red == 12) rFString = "C";
        else if (red == 13) rFString = "D";
        else if (red == 14) rFString = "E";
        else if (red == 15) rFString = "F";
        else rFString = String.valueOf(red);

        if (rred == 10) rSString = "A";
        else if (rred == 11) rSString = "B";
        else if (rred == 12) rSString = "C";
        else if (rred == 13) rSString = "D";
        else if (rred == 14) rSString = "E";
        else if (rred == 15) rSString = "F";
        else rSString = String.valueOf(rred);

        rFString = rFString + rSString;

        green = g / 16;
        rgreen = g % 16;

        if (green == 10) gFString = "A";
        else if (green == 11) gFString = "B";
        else if (green == 12) gFString = "C";
        else if (green == 13) gFString = "D";
        else if (green == 14) gFString = "E";
        else if (green == 15) gFString = "F";
        else gFString = String.valueOf(green);

        if (rgreen == 10) gSString = "A";
        else if (rgreen == 11) gSString = "B";
        else if (rgreen == 12) gSString = "C";
        else if (rgreen == 13) gSString = "D";
        else if (rgreen == 14) gSString = "E";
        else if (rgreen == 15) gSString = "F";
        else gSString = String.valueOf(rgreen);

        gFString = gFString + gSString;

        blue = b / 16;
        rblue = b % 16;

        if (blue == 10) bFString = "A";
        else if (blue == 11) bFString = "B";
        else if (blue == 12) bFString = "C";
        else if (blue == 13) bFString = "D";
        else if (blue == 14) bFString = "E";
        else if (blue == 15) bFString = "F";
        else bFString = String.valueOf(blue);

        if (rblue == 10) bSString = "A";
        else if (rblue == 11) bSString = "B";
        else if (rblue == 12) bSString = "C";
        else if (rblue == 13) bSString = "D";
        else if (rblue == 14) bSString = "E";
        else if (rblue == 15) bSString = "F";
        else bSString = String.valueOf(rblue);

        bFString = bFString + bSString;

        return "#" + rFString + gFString + bFString;
    }

    public static boolean isRuLocale(Context context) {
        Configuration sysConfig = context.getResources().getConfiguration();
        Locale curLocale = sysConfig.locale;
        if (curLocale.getLanguage().equals("ru")||curLocale.getLanguage().equals("uk")) {
            return  true;
        }else{
            return false;
        }

    }
}


