package ca.quanta.quantaevents.utils;

import android.content.Context;
import android.os.health.SystemHealthManager;
import android.widget.Toast;

public class ToastManager {
    private static Toast current;

    public static void show(Context context, String message) {
        if (context != null) {
            cancel();
            current = Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_LONG);
            current.show();
        }
    }

    // for toasts which need to be shown for a specific duration.
    public static void show(Context context, String message, int duration) {
        if (context != null) {
            cancel();
            current = Toast.makeText(context.getApplicationContext(), message, duration);
            current.show();
            System.out.println("Toast shown" + message);
        }
    }

    public static void cancel() {
        if (current != null) {
            current.cancel();
            current = null;
        }
    }
}