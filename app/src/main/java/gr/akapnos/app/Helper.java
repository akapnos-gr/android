package gr.akapnos.app;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.google.android.gms.maps.model.Marker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gr.akapnos.app.libraries.notification_center.NotificationCenter;
import gr.akapnos.app.map_classes.MapClusterItem;
import gr.akapnos.app.networking.RetrofitManager;
import gr.akapnos.app.objects.Store;
import gr.akapnos.app.utilities.OfflineDataStorage;
import okhttp3.ResponseBody;
import rx.Observer;
import trikita.log.Log;

import static gr.akapnos.app.TheApplication.getAppContext;
import static gr.akapnos.app.objects.Store.typeBAR;
import static gr.akapnos.app.objects.Store.typeCAFE;
import static gr.akapnos.app.objects.Store.typeFAVORITE;
import static gr.akapnos.app.objects.Store.typeNONE;
import static gr.akapnos.app.objects.Store.typeRESTAURANT;


public class Helper {
    public static Marker currentlyClickedMarker;
    public static MapClusterItem currentlyClickedClusterItem;

    private static Context application_context;
    private static Toast globalToast;

    private static ArrayList<Store> stores;
    private static int secondsToRefreshData = 259200; //three days
    public static boolean MAP_ICONS_WITH_BADGES = true;


    // ************************************ //
    // DEBUG values
    @SuppressWarnings("SameReturnValue")
    public static boolean isDebug() {
        return BuildConfig.DEBUG;
    }

    //call at application start
    static void initValues() {
        if (Helper.isDebug()) {
        }
    }
    // ************************************ //

    public static ArrayList<Store> getStores() {
        if (stores == null) {
            stores = OfflineDataStorage.getInstance().getStoredStores();
        }
        return stores;
    }
    public static void setStores(ArrayList<Store> _stores) {
        stores = _stores;
    }


    public static String stores_got_timestamp = "stores_got_timestamp";
    public static void downloadStores(Context context, final Runnable block) {
        Helper.downloadStores(context, false, block);
    }
    public static void downloadStores(Context context, boolean reset, final Runnable block) {
        boolean ok_to_download = reset || stores == null;

        if(!reset) {
            if (ok_to_download) {
                String stored = Helper.getValueFromPrefs(stores_got_timestamp);
                if (stored.length() > 0) {
                    long now = System.currentTimeMillis() / 1000;
                    ok_to_download = now - Long.parseLong(stored) > Helper.secondsToRefreshData;

                    Log.v("CHECK_DOWNLOAD_STORES: " + now + " - " + Long.parseLong(stored) + " > " + Helper.secondsToRefreshData + " ==> " + ok_to_download);
                }
            }
            if (!ok_to_download) {
                ok_to_download = Helper.getStores() == null || Helper.getStores().size() == 0;
            }
        }

        Log.v("ok_to_download = " + ok_to_download);
        if (ok_to_download) {
            new RetrofitManager(context, new Observer<ResponseBody>() {
                @Override
                public void onCompleted() {}
                @Override
                public void onError(Throwable e) {
                    Log.e("downloadStoresERROR", e.toString());
                    if (block != null) { block.run(); }
                }
                @Override
                public void onNext(ResponseBody response) {
                    boolean ok = false;
                    File file = new File(context.getCacheDir(), "stores.csv");
                    try {
                        InputStream inputStream = null;
                        OutputStream outputStream = null;
                        try {
                            byte[] fileReader = new byte[4096];
                            inputStream = response.byteStream();
                            outputStream = new FileOutputStream(file);
                            while (true) {
                                int read = inputStream.read(fileReader);
                                if (read == -1) {
                                    break;
                                }
                                outputStream.write(fileReader, 0, read);
                            }
                            outputStream.flush();
                            ok = true;
                        } catch (IOException e) {
                            Helper.showToast(e.getLocalizedMessage(), Toast.LENGTH_SHORT);
                        } finally {
                            if (inputStream != null) { inputStream.close(); }
                            if (outputStream != null) { outputStream.close(); }
                        }
                    } catch (IOException e) {
                        Helper.showToast(e.getLocalizedMessage(), Toast.LENGTH_SHORT);
                    }

                    if(ok) {
                        List<Map<String, String>> result = Helper.read(file);
                        if(result != null) {
                            result = Helper.cleanupArray(result);
                            Log.e("ResultsCount=" + result.size());

                            ArrayList<Store> tmp = new ArrayList<>();
                            for(Map<String, String> map : result) {
                                tmp.add(new Store(map));
                            }

                            Long tsLong = System.currentTimeMillis() / 1000;
                            Helper.setValueToPrefs(stores_got_timestamp, tsLong.toString());

                            Helper.setStores(tmp);
                            OfflineDataStorage.getInstance().storeStores(Helper.getStores());
                        }
                    }
                    if (block != null) {
                        block.run();
                    }
                }
            }).get_Stores();
        } else {
            if (block != null) {
                block.run();
            }
        }
    }

    private static List<Map<String, String>> cleanupArray(List<Map<String, String>> arr){
        ArrayList<Map<String, String>> tmp = new ArrayList<>();
        if(arr != null) {
            for (Map<String, String> map : arr){
                String status = map.get("Status");
                if (status != null && status.equals("publish")) {
                    String lang = map.get("Γλώσσες");
                    if (lang != null && lang.equals("Ελληνικά")) {
                        String lat = map.get("geolocation_lat");
                        String lon = map.get("geolocation_long");
                        if (lat != null && lat.length() > 0 && lon != null && lon.length() > 0) {
                            tmp.add(map);
                        }
                    }
                }
            }
        }
        return tmp;
    }

    public static List<Map<String, String>> read(File file){//} throws JsonProcessingException, IOException {

        List<Map<String, String>> response = new LinkedList<>();
        CsvMapper mapper = new CsvMapper();
        CsvSchema schema = CsvSchema.emptySchema().withHeader();
        try {
            MappingIterator<Map<String, String>> iterator = mapper.reader(Map.class)
                    .with(schema)
                    .readValues(file);
            while (iterator.hasNext()) {
                response.add(iterator.next());
            }
            return response;
        }
        catch (Exception e) {
            Helper.showToast(e.getLocalizedMessage(), Toast.LENGTH_SHORT);
        }
        return null;
    }

    public static Context appCtx() {
        if (application_context == null) {
            application_context = getAppContext();
        }
        return application_context;
    }

    public static void setValueToPrefs(String key, String value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getAppContext());
        SharedPreferences.Editor editor = prefs.edit();
        if (value == null) {
            editor.remove(key);
        } else {
            editor.putString(key, value);
        }
        editor.apply();
    }

    public static void setValuesToPrefs(Map<String, String> map) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(TheApplication.getAppContext());
        SharedPreferences.Editor editor = prefs.edit();

        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            editor.putString(key, map.get(key));
        }
        editor.apply();
    }

    public static String getValueFromPrefs(String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(TheApplication.getAppContext());
        return Helper.safeString(prefs.getString(key, null));
    }

    public static int colorFromHEX(String hex) {
        if (hex == null) {
            hex = "#000000";
        }
        if (hex.length() != 7 && hex.length() != 6) {
            hex = "#000000";
        }

        if (!hex.contains("#")) {
            hex = "#" + hex;
        }
        return Color.parseColor(hex);
    }


    public static void showToastGeneralError() {
        Helper.showToast(Helper.appCtx().getString(R.string.THERE_WAS_A_PROBLEM) + "\n" + Helper.appCtx().getString(R.string.PLEASE_TRY_AGAIN_LATER), Toast.LENGTH_LONG);
    }
    public static void showToast(String message) {
        Helper.showToast(message, Toast.LENGTH_SHORT);
    }
    public static void showToast(String message, int duration) {
        if (globalToast != null) {
            globalToast.cancel();
        }
        globalToast = Toast.makeText(Helper.appCtx(), message, duration);
        globalToast.show();
    }


    public static Drawable drawableForStoreType(Context context, int store_type) {
        int drawable_id = Helper.iconForStoreType(store_type);
        if (drawable_id != -1) {
            return context.getResources().getDrawable(drawable_id);
        }
        return null;
    }

    public static int iconForStoreType(int store_type) {
        return Helper.iconForStoreType(store_type, false);
    }
    public static int iconForStoreType(int store_type, boolean badge) {
        int drawable_id = -1;

        if(Helper.MAP_ICONS_WITH_BADGES) {
            if(badge) {
                drawable_id = R.drawable.map_pin_general;
            }
            else {
                switch (store_type) {
                    case typeBAR:
                        drawable_id = R.drawable.map_icon_bar;
                        break;
                    case typeCAFE:
                        drawable_id = R.drawable.map_icon_cafe;
                        break;
                    case typeRESTAURANT:
                        drawable_id = R.drawable.map_icon_restaurant;
                        break;
                    default:
                        break;
                }
            }
        }
        else {
            switch (store_type) {
                case typeBAR:
                    drawable_id = R.drawable.map_pin_bar;
                    break;
                case typeCAFE:
                    drawable_id = R.drawable.map_pin_cafe;
                    break;
                case typeRESTAURANT:
                    drawable_id = R.drawable.map_pin_restaurant;
                    break;
                default:
                    break;
            }
        }
        return drawable_id;
    }

    public static int colorForStoreType(int store_type) {
        String colorHex = "FFFFFF";
        switch (store_type) {
            case typeNONE:
            case typeBAR:
                colorHex = "0f9547";
                break;
            case typeCAFE:
                colorHex = "f69039";
                break;
            case typeRESTAURANT:
                colorHex = "4561d8";
                break;
        }
        return Helper.colorFromHEX(colorHex);
    }

    public static String titleForStoreType(int store_type) {
        String str = "";
        switch (store_type) {
            case typeFAVORITE:
                str = getAppContext().getResources().getString(R.string.FAVORITES);
                break;
            case typeNONE:
                str = getAppContext().getResources().getString(R.string.LBL_ALL_STORES);
                break;
            case typeBAR:
                str = getAppContext().getResources().getString(R.string.BARS);
                break;
            case typeCAFE:
                str = getAppContext().getResources().getString(R.string.CAFES);
                break;
            case typeRESTAURANT:
                str = getAppContext().getResources().getString(R.string.RESTAURANTS);
                break;
            default:
                break;
        }
        return str;
    }

    public static int screenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    public static void sortStores(ArrayList<Store> list) {
        if (list != null) {
            Collections.sort(list, new Comparator<Store>() {
                public int compare(Store obj1, Store obj2) {
                    return Float.compare(obj1.distance_to_user(), obj2.distance_to_user());
                }
            });
        }
    }

    public static void createAlertDialog(Activity activity, String title, String subtitle, String positiveText, String negativeText, DialogInterface.OnClickListener positiveListener, DialogInterface.OnClickListener negativeListener) {
        createAlertDialog(activity, title, subtitle, positiveText, negativeText, positiveListener, negativeListener, true);
    }
    public static void createAlertDialog(Activity activity, String title, String subtitle, String positiveText, String negativeText, DialogInterface.OnClickListener positiveListener, DialogInterface.OnClickListener negativeListener, boolean cancelable) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        if (title != null) {
            builder.setTitle(title);
        }
        if (subtitle != null) {
            builder.setMessage(subtitle);
        }
        builder.setPositiveButton(positiveText, positiveListener);
        builder.setNegativeButton(negativeText, negativeListener);
        builder.setCancelable(cancelable);
        builder.show();
    }

     public static void setVisibilityToTextView(TextView view) {
        if (view != null) {
            setVisibilityTo(view, view.getText().length() > 0, false);
        }
    }
    public static void setVisibilityTo(View view, boolean test) {
        setVisibilityTo(view, test, false);
    }
    public static void setVisibilityTo(View view, boolean test, boolean invisible) {
        if (view != null) {
            //noinspection ResourceType
            view.setVisibility(invisible ? getVisibleOrInvisible(test) : getVisibleOrGone(test));
        }
    }
    public static int getVisibleOrGone(boolean test) {
        return test ? View.VISIBLE : View.GONE;
    }
    public static int getVisibleOrInvisible(boolean test) {
        return test ? View.VISIBLE : View.INVISIBLE;
    }

    public static String safeString(String string) {
        return safeString(string, "");
    }
    public static String safeString(String string, String default_value) {
        return string == null ? default_value : string;
    }

    public static Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);

    }

    //====================================================================================================

    public static void hideKeyboard(Activity activity) {
        if (activity != null) {
            if (activity.getCurrentFocus() != null) {
                InputMethodManager imm = (InputMethodManager) (activity.getSystemService(Context.INPUT_METHOD_SERVICE));
                if (imm != null) {
                    imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
                }
            }
        }
    }



    public static void dumpBundle(Bundle bundle) {
        if (bundle != null) {
            Set<String> keys = bundle.keySet();
            Iterator<String> it = keys.iterator();
            Log.e("Dumping Intent", "--START--");
            while (it.hasNext()) {
                String key = it.next();
                Log.e("Dumping Intent", "         [" + key + "=" + bundle.get(key) + "]");
            }
            Log.e("Dumping Intent", "--END--");
        } else {
            Log.e("Dumping Intent", "--NULL--");
        }
    }

    //----------------

    private static HashMap<String, String> fav_stores;
    public static HashMap<String, String> favStores() {
        if(fav_stores == null) {
            fav_stores = OfflineDataStorage.getInstance().getFavoriteStores();
        }
        return fav_stores;
    }
    public static boolean isFavorite(Store store) {
        if(store != null) {
            return Helper.favStores().get(store.getStore_id()) != null;
        }
        return false;
    }
    public static void toggleFavorite(Context context, Store store) {
        boolean is_fav = Helper.isFavorite(store);

        if(is_fav) { Helper.favStores().remove(store.getStore_id()); }
        else { Helper.favStores().put(store.getStore_id(), store.getStore_id()); }

//        NSLog(@"Favs: %@",[self favStores]);
        NotificationCenter.postNotification(context, NotificationCenter.NotificationType.FavStoresUpdated);

        OfflineDataStorage.getInstance().saveFavoriteStores(Helper.favStores());
    }
}