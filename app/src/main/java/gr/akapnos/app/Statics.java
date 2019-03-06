package gr.akapnos.app;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Patterns;

import com.google.gson.internal.LinkedTreeMap;

import java.util.LinkedHashMap;

import gr.akapnos.app.objects.ImageModel;
import trikita.log.Log;


public class Statics {
    public static final String LANGUAGE_SELECTION_WAIT = "LangSelectionWait";
    public static final String GET_IMAGE_SOURCE_DIALOG="ImageDialog";

    public static final int INDEX_SUGGEST = 10;
    public static final int INDEX_REPORT = 11;
    public static final int INDEX_ABOUT_US = 12;
    public static final int INDEX_FB_LINK = 13;

    public static String imageFrom(Object obj) {
        if(obj instanceof ImageModel) {
            return ((ImageModel)obj).getImage();
        } else if(obj instanceof String) {
            return (String)obj;
        } else if(obj instanceof LinkedHashMap) {
            return (String) ((LinkedHashMap)obj).get("image");
        } else if(obj instanceof LinkedTreeMap) {
            return (String) ((LinkedTreeMap)obj).get("image");
        }
        return "";
    }

    public static boolean openURL(Context context, String url, boolean notify_user_on_error) {
        if(url != null && Patterns.WEB_URL.matcher(url).matches()) {
            try {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(browserIntent);
                return true;
            }
            catch(Exception e) {
                Log.e("OpenURL exception: ");
                e.printStackTrace();

                if(notify_user_on_error) {
                    Helper.showToast(e.getLocalizedMessage());
                }
            }
        }
        else {
            if(notify_user_on_error) {
                Helper.showToast(context.getResources().getString(R.string.URL_INVALID));
            }
            Log.e("url = " + url);
        }
        return false;
    }
}
