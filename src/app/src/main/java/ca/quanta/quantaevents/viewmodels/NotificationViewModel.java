package ca.quanta.quantaevents.viewmodels;

import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.functions.FirebaseFunctions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import ca.quanta.quantaevents.models.ExternalNotification;
import ca.quanta.quantaevents.models.ExternalUndismissedNotification;

/**
 * View-model for managing notification-related data and cloud functions.
 */
public class NotificationViewModel extends ViewModel {
    private FirebaseFunctions functions = FirebaseFunctions.getInstance();

    /**
     * Calls the createNotification cloud function, creating and sending a notification.
     * @param userId    UUID identifying sender.
     * @param deviceId  UUID identifying sender's device.
     * @param message   Notification message.
     * @param title     Notification title.
     * @param eventId   UUID identifying the event associated with the notification.
     * @param waited    Boolean that's true when the user wishes to send to people on the event's waitlist.
     * @param cancelled Boolean that's true when the user wishes to send to people on the event's canceled list.
     * @param selected  Boolean that's true when the user wishes to send to people on the event's selected list.
     * @param finale    Boolean that's true when the user wishes to send to people on the event's final list
     * @return UUID identifying the newly created notification.
     */
    public Task<UUID> createNotification(UUID userId, UUID deviceId, String message,
                                         String title, String eventId, Boolean waited,
                                         Boolean cancelled, Boolean selected, Boolean finale) {
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
        payload.put("final", finale);
        data.put("data", payload);

        return functions
                .getHttpsCallable("createNotification")
                .call(data)
                .onSuccessTask(callResult -> {
                    Map<String, Object> result = (Map<String, Object>) callResult.getData();
                    return Tasks.forResult(UUID.fromString((String) result.get("notificationId")));
                });
    }

    /**
     * Calls the getAllNotifications cloud function, getting all notifications send by a user.
     * @param userId UUID identifying the invoker.
     * @param deviceId UUID identifying the invoker's device.
     * @param sentById UUID identifying the target user.
     * @return ArrayList of external notifications sent by a user.
     */
    public Task<ArrayList<ExternalNotification>> getAllNotifications(UUID userId, UUID deviceId, UUID sentById) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId.toString());
        data.put("deviceId", deviceId.toString());

        Map<String, Object> payload = new HashMap<>();
        payload.put("sentById", sentById.toString());
        data.put("data", payload);

        return functions
                .getHttpsCallable("getAllNotifications")
                .call(data)
                .onSuccessTask(callResult -> {
                    List<Map<String, Object>> notificationObjects = (List<Map<String, Object>>) callResult.getData();
                    ArrayList<ExternalNotification> notifications = new ArrayList<>();

                    for (Map<String, Object> notificationObject : notificationObjects) {
                        String title = (String) notificationObject.get("title");
                        String message = (String) notificationObject.get("message");

                        notifications.add(new ExternalNotification(title, message));
                    }

                    return Tasks.forResult(notifications);
                });
    }

    /**
     * Calls the getAllUndismissedNotifications cloud function, getting all undismissed notifications sent to a user.
     * @param userId UUID identifying the user.
     * @param deviceId UUID identifying the user's device.
     * @return ArrayList of undismissed external notifications sent to the user.
     */
    public Task<ArrayList<ExternalUndismissedNotification>> getAllUndismissedNotifications(UUID userId, UUID deviceId) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId.toString());
        data.put("deviceId", deviceId.toString());

        Map<String, Object> payload = new HashMap<>();
        data.put("data", payload);

        return functions
                .getHttpsCallable("getAllUndismissedNotifications")
                .call(data)
                .onSuccessTask(callResult -> {
                    List<Map<String, Object>> notificationObjects = (List<Map<String, Object>>) callResult.getData();
                    ArrayList<ExternalUndismissedNotification> notifications = new ArrayList<>();

                    for (Map<String, Object> notificationObject : notificationObjects) {
                        UUID notificationId = UUID.fromString((String) notificationObject.get("notificationId"));
                        UUID eventId = UUID.fromString((String) notificationObject.get("eventId"));

                        String title = (String) notificationObject.get("title");
                        String message = (String) notificationObject.get("message");

                        String kind = (String) notificationObject.get("kind");
                        Boolean lotterySelected = (Boolean) notificationObject.get("lotterySelected");

                        notifications.add(new ExternalUndismissedNotification(notificationId, eventId, title, message, kind, lotterySelected));
                    }

                    return Tasks.forResult(notifications);
                });
    }

    /**
     * Calls the dismissNotification cloud function, dismissing a notification.
     * @param userId UUID identifying the user.
     * @param deviceId UUID identifying the user's device.
     * @param notificationId UUID identifying dismissed notification.
     * @return Void task.
     */
    public Task<Void> dismissNotification(UUID userId, UUID deviceId, UUID notificationId) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId.toString());
        data.put("deviceId", deviceId.toString());

        Map<String, Object> payload = new HashMap<>();
        payload.put("notificationId", notificationId.toString());

        data.put("data", payload);

        return functions
                .getHttpsCallable("dismissNotification")
                .call(data)
                .onSuccessTask(callResult -> {
                    return Tasks.forResult(null);
                });
    }
}
