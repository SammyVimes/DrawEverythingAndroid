package ru.edikandco.draweverything.dialog;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import ru.edikandco.draweverything.R;

/**
 * Created by Semyon on 12.05.2015.
 */
public class MyProgressDialog extends DialogFragment {

    private ProgressDialog progressDialog = null;
    private int max;
    private Context context;

    public MyProgressDialog() {
    }

    public int getMax() {
        return max;
    }

    public void setMax(final int max) {
        this.max = max;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(final Context context) {
        this.context = context;
    }

    public static MyProgressDialog createDialog(final Context context, final int max) {
        MyProgressDialog myProgressDialog = new MyProgressDialog();
        myProgressDialog.setMax(max);
        myProgressDialog.setContext(context);
        return myProgressDialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.setMax(max);
        progressDialog.setProgress(0);
        return progressDialog;
    }

    public void setProgress(final int progress) {
        progressDialog.setProgress(progress);
    }

}