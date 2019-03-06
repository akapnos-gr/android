package gr.akapnos.app.utilities;

import android.app.ProgressDialog;
import android.content.Context;
import trikita.log.Log;

import gr.akapnos.app.R;

public class LoadingProgress {
    static ProgressDialog progress;
    static ProgressDialog progressBar;

    static Context context;
    static boolean is_cancelable;

    public static void show(Context ctx) {
        show(ctx, true, true);
    }
    public static void show(Context ctx, boolean cancelable) {
        show(ctx, cancelable, true);
    }
    public static void show(Context ctx, boolean cancelable, boolean indeterminate) {
        try {
            if (progress != null) {
                if (progress.isShowing() && indeterminate) {
                    return;
                }
            }
            else if(progressBar != null) {
                if (progressBar.isShowing() && !indeterminate) {
                    return;
                }
            }
            context = ctx;
            is_cancelable = cancelable;

            if(indeterminate) {
                progress = new ProgressDialog(context);
                progress.setTitle(ctx.getString(R.string.LOADING));
                progress.setMessage(ctx.getString(R.string.PLEASE_WAIT));
                progress.setCancelable(is_cancelable);
                progress.setIndeterminate(true);
                progress.show();

                Log.v("LOADING PROGRESS", "INDETERMINATE1:" + progress.isIndeterminate());
            }
            else {
                progressBar = new ProgressDialog(context);
//                progressBar.setTitle(ctx.getString(R.string.LOADING));
//                progressBar.setText_message(ctx.getString(R.string.PLEASE_WAIT));
                progressBar.setCancelable(is_cancelable);
                progressBar.setIndeterminate(false);
                progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressBar.setProgress(0);
                progressBar.setMax(100);
                progressBar.show(ctx, ctx.getString(R.string.LOADING), ctx.getString(R.string.PLEASE_WAIT), indeterminate);

                Log.v("LOADING PROGRESS", "INDETERMINATE2:" + progressBar.isIndeterminate() + "--"+ indeterminate);
            }
        }
        catch (Exception e) {
            Log.e("LoadingProgress", "ShowERROR: " + e.getLocalizedMessage());
        }
    }

    public static void hide() {
        try {
            if (progress != null) {
                progress.dismiss();
            }
            if (progressBar != null) {
                progressBar.dismiss();
            }
        }
        catch (Exception e) {
            Log.e("LoadingProgress", "HideERROR: " + e.getLocalizedMessage());
        }
    }

    public static void setTheProgress(int prog) {
        Log.e("LoadingProgress", "SETPROGRESS: " + progressBar + " - " + prog);

        if(progressBar != null) {
            progressBar.setProgress(prog);
        }
    }
}
