package ca.quanta.quantaevents.viewmodels;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NotificationViewModel extends ViewModel {

    private FirebaseFunctions functions = FirebaseFunctions.getInstance();

    public Task<String> createEventNotification(UUID userId, UUID deviceId, String message,
                                    String title, String eventId, Boolean waited,
                                           Boolean cancelled, Boolean selected) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId.toString());
        data.put("deviceId", deviceId.toString());

        Map<String, Object> payload = new HashMap<>();
        payload.put("message", message);
        payload.put("title", title);
        payload.put("eventId", eventId);
        payload.put("waited", waited);
        payload.put("cancelled", cancelled);
        payload.put("selected", selected);
        data.put("data", payload);

        Log.d("TAG", message);

        return functions
                .getHttpsCallable("createEventNotification")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        Map<String, Object> result = (Map<String, Object>) task.getResult().getData();
                        return (String) result.get("notificationId");
                    }
                });
    }

}
