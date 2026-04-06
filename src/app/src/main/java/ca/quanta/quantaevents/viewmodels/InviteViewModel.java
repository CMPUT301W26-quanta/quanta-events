package ca.quanta.quantaevents.viewmodels;

import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.functions.FirebaseFunctions;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * View-model for accepting / rejecting lottery invitations to events.
 */
public class InviteViewModel extends ViewModel {
    // Initialize an instance of cloud functions
    private FirebaseFunctions functions = FirebaseFunctions.getInstance();

    /**
     * Calls the inviteAccept cloud function, accepting a lottery invite.
     *
     * @param userId   UUID to identify user.
     * @param deviceId UUID to identify the user's device.
     * @param eventId  UUID of the event to accept the invite on.
     * @return Nothing.
     */
    public Task<Void> inviteAccept(UUID userId, UUID deviceId, UUID eventId) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId.toString());
        data.put("deviceId", deviceId.toString());

        Map<String, Object> payload = new HashMap<>();
        payload.put("eventId", eventId.toString());
        data.put("data", payload);

        return functions
                .getHttpsCallable("inviteAccept")
                .call(data)
                .onSuccessTask(callResult -> {
                    return Tasks.forResult(null);
                });
    }

    /**
     * Calls the inviteReject cloud function, rejecting a lottery invite.
     *
     * @param userId   UUID to identify user.
     * @param deviceId UUID to identify the user's device.
     * @param eventId  UUID of the event to reject the invite on.
     * @return Nothing.
     */
    public Task<Void> inviteReject(UUID userId, UUID deviceId, UUID eventId) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId.toString());
        data.put("deviceId", deviceId.toString());

        Map<String, Object> payload = new HashMap<>();
        payload.put("eventId", eventId.toString());
        data.put("data", payload);

        return functions
                .getHttpsCallable("inviteReject")
                .call(data)
                .onSuccessTask(callResult -> {
                    return Tasks.forResult(null);
                });
    }

    /**
     * Calls the coInviteAccept cloud function, assigning a user as co-organizer for an event
     *
     * @param userId   UUID identifying the user.
     * @param deviceId UUID identifying user's device.
     * @param eventId  UUID identifying the event.
     * @return Void task.
     */
    public Task<Void> coInviteAccept(UUID userId, UUID deviceId, UUID eventId) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId.toString());
        data.put("deviceId", deviceId.toString());

        Map<String, Object> payload = new HashMap<>();
        payload.put("eventId", eventId.toString());
        data.put("data", payload);

        return functions
                .getHttpsCallable("coInviteAccept")
                .call(data)
                .onSuccessTask(callResult -> {
                    return Tasks.forResult(null);
                });
    }
}