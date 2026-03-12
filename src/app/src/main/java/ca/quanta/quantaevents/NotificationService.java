package ca.quanta.quantaevents;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.messaging.FirebaseMessagingService;

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
