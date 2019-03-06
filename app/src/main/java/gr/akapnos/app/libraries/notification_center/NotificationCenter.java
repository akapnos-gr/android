package gr.akapnos.app.libraries.notification_center;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import java.util.HashMap;
import java.util.Map;


public class NotificationCenter {
    public enum NotificationType {
        UserMustLoginAgain,
        UserLoggedIn,
        UserChangedStatus,
        RegisteredToken,
        FavStoresUpdated,
        OfflineStoreSearch,
        AppEnteredBackground
    }

    public static void addObserver(Context context, NotificationType notification, BroadcastReceiver responseHandler) {
        LocalBroadcastManager.getInstance(context).registerReceiver(responseHandler, new IntentFilter(notification.name()));
    }

    public static void removeObserver(Context context, BroadcastReceiver responseHandler) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(responseHandler);
    }
    public static void postNotification(Context context, NotificationType notification) {
        postNotification(context, notification, null);
    }

    public static void postNotification(Context context, NotificationType notification, HashMap<String, String> params) {
        Intent intent = new Intent(notification.name());
        if(params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                intent.putExtra(key, value);
            }
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}