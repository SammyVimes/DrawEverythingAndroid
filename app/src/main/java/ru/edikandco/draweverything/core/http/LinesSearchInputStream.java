package ru.edikandco.draweverything.core.http;

import android.util.Log;


import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import ru.edikandco.draweverything.core.util.IoUtils;

/**
 * Created by Semyon Danilov on 11.06.2014.
 */
public class LinesSearchInputStream extends FilterInputStream {

    public static final String TAG = "LinesSearchInputStream";

    public static final int SEARCHING = 0;
    public static final int FOUND = 1;
    public static final int NOT_FOUND = 2;

    private int state = SEARCHING;

    private byte[] prevLoaded;

    private byte[] desire;

    private byte[] delimiter;

    private boolean hasFoundDesired = false;


    @Override
    public int read(byte[] b) throws IOException {
        int red = in.read(b);
        if (red == -1) {
            if (!hasFoundDesired) {
                state = NOT_FOUND;
            } else {
                state = FOUND;
            }
        } else {
            if (!hasFoundDesired) {
                hasFoundDesired = searchFor(red, b, desire);
                if (hasFoundDesired) {
                    Log.d(TAG, "Found desired");
                    byte[] tmp = new byte[red - foundOffset];
                    IoUtils.copyArray(b, foundOffset, red, tmp, 0);
                    updateFound(red - foundOffset, tmp);
                    foundOffset = 0;
                }
            } else {
                updateFound(red, b);
            }
        }
        return state;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int red = in.read(b, off, len);
        if (red == -1) {
            if (!hasFoundDesired) {
                state = NOT_FOUND;
            } else {
                state = FOUND;
            }
        } else {
            if (!hasFoundDesired) {
                hasFoundDesired = searchFor(red, b, desire);
                if (hasFoundDesired) {
                    prevLoaded = new byte[red - foundOffset];
                    IoUtils.copyArray(b, foundOffset, red, prevLoaded, 0);
                }
            } else {
                updateFound(red ,b);
            }
        }
        return state;
    }


    public byte[] getResult() {
        return prevLoaded;
    }

    private void updateFound(final int red, final byte[] bytes) {
        boolean foundDelimiter = searchFor(red, bytes, delimiter);
        if (prevLoaded == null) {
            prevLoaded = new byte[0];
        }
        int prevLen = prevLoaded.length;
        int newLen = prevLen + red;
        byte[] newArray = null;
        if (foundDelimiter) {
            newArray = new byte[prevLen + foundOffset];
            IoUtils.copyArray(prevLoaded, 0, newArray, 0);
            IoUtils.copyArray(bytes, 0, foundOffset, newArray, prevLen);
            state = FOUND;
        } else {
            newArray = new byte[newLen];
            IoUtils.copyArray(prevLoaded, 0, newArray, 0);
            IoUtils.copyArray(bytes, 0, red, newArray, prevLen);
        }
        prevLoaded = newArray;
    }

    private int successMatched = 0;
    private int foundOffset = -1;

    private boolean searchFor(final int red, final byte[] bytes, final byte[] desire) {
        boolean fullFound = false;
        for (int i = 0; i < red; i++) {
            byte cur = bytes[i];
            boolean wrong = false;
            boolean reachedEnd = false;
            int a = i;
            for (int j = successMatched; j < desire.length; j++) {
                if (cur != desire[j]) {
                    wrong = true;
                    break;
                }
                a++;
                if (j == desire.length - 1) {
                    fullFound = true;
                }
                if (a < red) {
                    cur = bytes[a];
                } else {
                    Log.d(TAG, "Splitted content, beware");
                    successMatched = j + 1;
                    reachedEnd = true;
                    break;
                }
            }
            if (wrong) {
                successMatched = 0;
            }
            if (fullFound) {
                successMatched = 0;
                foundOffset = a;
                break;
            }
            if (reachedEnd) {
                break;
            }
        }
        if (fullFound) {
            Log.d(TAG, "found");
            return true;
        }
        return false;
    }

    public LinesSearchInputStream(final InputStream in, final String desire, final String delimiter) {
        this(in, desire.getBytes(), delimiter.getBytes());
    }

    public LinesSearchInputStream(final InputStream in, final byte[] desire, final byte[] delimiter) {
        super(in);
        this.desire = desire;
        this.delimiter = delimiter;
    }

}
