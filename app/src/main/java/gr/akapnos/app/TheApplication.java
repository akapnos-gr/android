package gr.akapnos.app;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.multidex.MultiDexApplication;
import android.support.v4.util.LruCache;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.jenzz.appstate.AppStateListener;
import com.jenzz.appstate.AppStateMonitor;
import gr.akapnos.app.libraries.notification_center.NotificationCenter;
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