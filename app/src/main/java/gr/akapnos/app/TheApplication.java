package gr.akapnos.app;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.multidex.MultiDexApplication;
import android.support.v4.util.LruCache;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.jenzz.appstate.AppStateListener;
import com.jenzz.appstate.AppStateMonitor;

import gr.akapnos.app.libraries.notification_center.NotificationCenter;
import gr.akapnos.app.objects.Store;
import trikita.log.Log;

import static gr.akapnos.app.libraries.notification_center.NotificationCenter.NotificationType.AppEnteredBackground;

public class TheApplication extends MultiDexApplication {
    private static Context context;
    public static boolean in_foreground = false;

    @Override
    public void onCreate() {
        super.onCreate();
        TheApplication.context = getApplicationContext();
        Helper.initValues();

        AppStateMonitor appStateMonitor = AppStateMonitor.create(this);
        appStateMonitor.addListener(new AppStateListener() {
            @Override
            public void onAppDidEnterForeground() {
                Log.e("AppState", "Entered foreground");
                in_foreground = true;
            }
            @Override
            public void onAppDidEnterBackground() {
                Log.e("AppState", "Entered background");
                in_foreground = false;
                NotificationCenter.postNotification(context, AppEnteredBackground);
            }
        });
        appStateMonitor.start();

        if (!BuildConfig.DEBUG) {
            Log.usePrinter(Log.ANDROID, false); // from now on Log.d etc do nothing and is likely to be optimized with JIT
        }
    }

    public static Context getAppContext() {
        return TheApplication.context;
    }


    private static LruCache<String, BitmapDescriptor> cache;
    public static void initCache() {
        if(cache == null) {
            //Use 1/8 of available memory
            cache = new LruCache<>((int) (Runtime.getRuntime().maxMemory() / 1024 / 8));
        }
    }
    public static BitmapDescriptor getBitmapDescriptor(int icon_id) {
        String resource_name = String.valueOf(icon_id);
        BitmapDescriptor result = cache.get(resource_name);
        if (result == null) {
            Drawable drawable = getAppContext().getResources().getDrawable(icon_id);
            result = BitmapDescriptorFactory.fromBitmap(getMarkerIconFromDrawable(drawable, icon_id));
            cache.put(resource_name, result);
        }
        return result;
    }
    private static Bitmap getMarkerIconFromDrawable(Drawable drawable, int icon_id) {
        Canvas canvas = new Canvas();
        Bitmap bitmap0 = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Bitmap bitmap = Helper.getResizedBitmap(bitmap0, 140);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    //store
    public static BitmapDescriptor getBitmapDescriptor(Context ctx, Store store) {
        boolean is_cafe = store.isCafe();
        boolean is_bar = store.isBar();
        boolean is_restaurant = store.isRestaurant();

        int icon_id = R.drawable.map_pin_general;

        String resource_name = icon_id + "_" + (is_cafe ? "1":"0") + (is_bar ? "1":"0") + (is_restaurant ? "1":"0");
        BitmapDescriptor result = cache.get(resource_name);
        if (result == null) {
            Bitmap bmp = getMarkerIconFromDrawable2(ctx, is_cafe, is_bar, is_restaurant);
            if(bmp != null) {
                result = BitmapDescriptorFactory.fromBitmap(bmp);
                cache.put(resource_name, result);
            }
        }
        return result;
    }
    private static Bitmap getMarkerIconFromDrawable2(Context ctx, boolean is_cafe, boolean is_bar, boolean is_restaurant) {
        try {
            ViewGroup viewMarker = (ViewGroup)((LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_general_map_marker, null, false);
            Helper.setVisibilityTo(viewMarker.findViewById(R.id.img_cafe), is_cafe);
            Helper.setVisibilityTo(viewMarker.findViewById(R.id.img_bar), is_bar);
            Helper.setVisibilityTo(viewMarker.findViewById(R.id.img_restaurant), is_restaurant);
            return createDrawableFromView(ctx, viewMarker);
        }
        catch (Exception ignored) {}
        return null;
    }
    public static Bitmap createDrawableFromView(Context context, ViewGroup view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int ww = displayMetrics.widthPixels;
        int hh = displayMetrics.heightPixels;

        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        view.measure(ww, hh);
        view.layout(0, 0, ww, hh);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(),view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LocaleManager.setLocale(this);
    }
}