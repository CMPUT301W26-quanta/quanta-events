package ca.quanta.quantaevents.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import ca.quanta.quantaevents.models.Notification;

/**
 * View-model for managing notification-related data and cloud functions.
 */
public class NotificationViewModel extends ViewModel {
    private FirebaseFunctions functions = FirebaseFunctions.getInstance();

    /**
     * Calls the createNotification cloud function, creating and sending a notification.
     * @param userId UUID identifying sender.
     * @param deviceId UUID identifying sender's device.
     * @param message Notification message.
     * @param title Notification title.
     * @param eventId UUID identifying the event associated with the notification.
     * @param waited Boolean that's true when the user wishes to send to people on the event's waitlist.
     * @param cancelled Boolean that's true when the user wishes to send to people on the event's canceled list.
     * @param selected Boolean that's true when the user wishes to send to people on the event's selected list.
     * @return UUID identifying the newly created notification.
     */
    public Task<UUID> createNotification(UUID userId, UUID deviceId, String message,
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

        return functions
                .getHttpsCallable("createNotification")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, UUID>() {
                    @Override
                    public UUID then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        Map<String, Object> result = (Map<String, Object>) task.getResult().getData();
                        return UUID.fromString((String) result.get("notificationId"));
                    }
                });
    }

    public Task<ArrayList<Notification>> getAllNotifications(UUID userId, UUID deviceId, UUID sentById) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId.toString());
        data.put("deviceId", deviceId.toString());

        Map<String, Object> payload = new HashMap<>();
        payload.put("sentById", sentById.toString());
        data.put("data", payload);

        return functions
                .getHttpsCallable("getAllNotifications")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, ArrayList<Notification>>() {
                    @Override
                    public ArrayList<Notification> then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        List<Map<String, Object>> notificationObjects = (List<Map<String, Object>>) task.getResult().getData();
                        ArrayList<Notification> notifications = new ArrayList<>();

                        for (Map<String, Object> notificationObject : notificationObjects) {
                            UUID eventId = UUID.fromString((String) notificationObject.get("eventId"));

                            String title = (String) notificationObject.get("title");
                            String message = (String) notificationObject.get("message");

                            Boolean waited = (Boolean) notificationObject.get("waited");
                            Boolean selected = (Boolean) notificationObject.get("selected");
                            Boolean cancelled = (Boolean) notificationObject.get("cancelled");

                            notifications.add(new Notification(eventId, title, message, waited, selected, cancelled));
                        }

                        return notifications;
                    }
                });
    }
}