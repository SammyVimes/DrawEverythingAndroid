package ru.edikandco.draweverything.core.http;

import android.content.res.Resources;


import org.apache.http.Header;
import org.apache.http.HttpResponse;

import java.io.InputStream;

import ru.edikandco.draweverything.core.util.IoUtils;

/**
 * Created by Semyon Danilov on 16.05.2014.
 */
public class HttpBytesReader {
    public static final String TAG = "HttpBytesReader";

    private final Resources mResources;
    private final HttpStreamReader mHttpStreamReader;

    public HttpBytesReader(HttpStreamReader httpStreamReader, Resources resources) {
        this.mHttpStreamReader = httpStreamReader;
        this.mResources = resources;
    }

    public byte[] fromUri(String uri) throws HttpRequestException {
        return this.fromUri(uri, null);
    }

    public byte[] fromUri(String uri, Header[] customHeaders) throws HttpRequestException {
        return this.fromUri(uri, customHeaders, null, null);
    }

    public byte[] fromUri(String uri, Header[] customHeaders, IProgressChangeListener listener, ICancelled task) throws HttpRequestException {
        HttpStreamModel streamModel = this.mHttpStreamReader.fromUri(uri, customHeaders, listener, task);

        try {
            byte[] result = this.convertStreamToBytes(streamModel.stream);
            return result;
        } finally {
            ExtendedHttpClient.releaseRequestResponse(streamModel.request, streamModel.response);
        }
    }

    public byte[] fromResponse(HttpResponse response) throws HttpRequestException {
        return this.fromResponse(response, null, null);
    }

    public byte[] fromResponse(HttpResponse response, IProgressChangeListener listener, ICancelled task) throws HttpRequestException {
        InputStream stream = this.mHttpStreamReader.fromResponse(response, listener, task);

        try {
            byte[] result = this.convertStreamToBytes(stream);
            return result;
        } finally {
            ExtendedHttpClient.releaseResponse(response);
        }
    }

    public void removeIfModifiedForUri(String uri) {
        this.mHttpStreamReader.removeIfModifiedForUri(uri);
    }

    private byte[] convertStreamToBytes(InputStream stream) throws HttpRequestException {
        try {
            byte[] result = IoUtils.convertStreamToBytes(stream);
            return result;
        } catch (Exception e) {
            throw new HttpRequestException(e.getMessage());
        }
    }
}
