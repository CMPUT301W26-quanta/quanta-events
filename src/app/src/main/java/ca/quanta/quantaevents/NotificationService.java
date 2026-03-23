package ca.quanta.quantaevents;

import android.Manifest;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

import ca.quanta.quantaevents.stores.SessionStore;

public class NotificationService extends FirebaseMessagingService {
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        SharedPreferences prefs = getSharedPreferences(SessionStore.PREFS_NAME, Application.MODE_PRIVATE);
        String userId = prefs.getString(SessionStore.KEY_USER_ID, null);
        String deviceId = prefs.getString(SessionStore.KEY_DEVICE_ID, null);
        updateToken(token, userId, deviceId).addOnFailureListener(Throwable::printStackTrace);
        Log.d("ZZZZZZZZZZZZZZZZZ", "onNewToken!!!!!!!!!!!!!!");
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        message.getData();
        Log.d("ZZZZZZZZZZZZZZZZZ", "onReceiveToken!!!!!!!!!!!!!!");
    }

    private void showNotification() {
        String channelId = "default_channel";
        CharSequence name = "Default Channel";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;

        NotificationChannelCompat channel = new NotificationChannelCompat.Builder(channelId, importance)
                .setName(name)
                .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.createNotificationChannel(channel);

        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Hello")
                .setContentText("This is a notification")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        notificationManager.notify(1, notification);
    }

    public static Task<Boolean> updateToken(@NonNull String token, String userId, String deviceId) {
        Map<String, Object> data = new HashMap<>();

        data.put("userId", userId);
        data.put("deviceId", deviceId);

        Map<String, Object> payload = new HashMap<>();
        payload.put("token", token);
        data.put("data", payload);

        return FirebaseFunctions.getInstance()
                .getHttpsCallable("setToken")
                .call(data)
                .continueWith(_task -> true);
    }
}
