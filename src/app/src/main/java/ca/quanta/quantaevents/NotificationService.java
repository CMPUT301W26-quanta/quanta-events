package ca.quanta.quantaevents;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.pm.PackageManager;

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

/**
 * Service for handling firebase cloud messaging and app notifications.
 */
public class NotificationService extends FirebaseMessagingService {

    private SessionStore sessionStore;

    @Override
    public void onCreate() {
        super.onCreate();
        sessionStore = new SessionStore(getApplication());
    }

    /**
     * Listener which activates on creation of a new messaging token.
     *
     * @param token The token used for sending messages to this application instance.
     */
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        String userId = sessionStore.getUserId().getValue();
        String deviceId = sessionStore.getDeviceId().getValue();
        updateToken(token, userId, deviceId).addOnFailureListener(Throwable::printStackTrace);
    }

    /**
     * Listener which activates when a device receives a notification message.
     *
     * @param message Task object containing message data.
     */
    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        RemoteMessage.Notification notification = message.getNotification();
        if (notification != null) {
            showNotification(notification.getTitle(), notification.getBody());
        }
    }

    /**
     * Shows notifications in the device notification wall.
     *
     * @param title Title of the notification.
     * @param body  Message text of the notification.
     */
    private void showNotification(String title, String body) {
        String channelId = "default_channel";
        CharSequence name = "Default Channel";
        int importance = NotificationManager.IMPORTANCE_HIGH;

        NotificationChannelCompat channel = new NotificationChannelCompat.Builder(channelId, importance)
                .setName(name)
                .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.createNotificationChannel(channel);

        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
                .build();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        notificationManager.notify(1, notification);
    }

    /**
     *
     * @param token    Device's cloud messaging token.
     * @param userId   UUID identifying the user.
     * @param deviceId UUID identifying the user's device.
     * @return Boolean task object confirming successful execution.
     */
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