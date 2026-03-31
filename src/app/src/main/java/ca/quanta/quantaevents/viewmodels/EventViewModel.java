package ca.quanta.quantaevents.viewmodels;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import ca.quanta.quantaevents.models.Event;

/**
 * View-model for managing event-related data and cloud functions.
 */
public class EventViewModel extends ViewModel {
    // Initialize an instance of cloud functions
    private FirebaseFunctions functions = FirebaseFunctions.getInstance();

    public enum Fetch {
        ALL("all"),
        CREATED("created"),
        AVAILABLE("available"),
        IN("in"),
        HISTORY("history");

        private final String value;

        Fetch(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum SortBy {
        REGISTRATION_END("registrationEnd"),
        REGISTRATION_START("registrationStart"),
        NAME("name");

        private final String value;

        SortBy(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    /**
     * Calls the createEvent cloud function and adds an event to the database.
     *
     * @param userId                UUID to identify user.
     * @param deviceId              UUID to identify user's device.
     * @param registrationStartTime ISO-8601 datetime with offset (UTC preferred).
     * @param registrationEndTime   ISO-8601 datetime with offset (UTC preferred).
     * @param eventTime             ISO-8601 datetime with offset (UTC preferred).
     * @param eventName             Name of the event.
     * @param eventDescription      Description of the event.
     * @param eventCategory         Category (optional).
     * @param eventGuidelines       Guidelines (optional).
     * @param geolocation           Whether geolocation is enabled.
     * @param eventCapacity         Entrant capacity.
     * @param location              Location of the event.
     * @param registrationLimit     Waitlist capacity (optional).
     * @param imageId               UUID identifying the image for the event (optional).
     * @return UUID assigned to the newly created event.
     */
    public Task<UUID> createEvent(UUID userId, UUID deviceId, String registrationStartTime,
                                  String registrationEndTime, String eventTime,
                                  String eventName, String eventDescription,
                                  String eventCategory, String eventGuidelines,
                                  boolean geolocation, int eventCapacity,
                                  String location, Integer registrationLimit, UUID imageId) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId.toString());
        data.put("deviceId", deviceId.toString());

        Map<String, Object> payload = new HashMap<>();
        payload.put("registrationStartTime", registrationStartTime);
        payload.put("registrationEndTime", registrationEndTime);
        payload.put("eventTime", eventTime);
        payload.put("eventName", eventName);
        payload.put("eventDescription", eventDescription);
        payload.put("eventCategory", eventCategory);
        payload.put("eventGuidelines", eventGuidelines);
        payload.put("geolocation", geolocation);
        payload.put("eventCapacity", eventCapacity);
        payload.put("location", location);
        payload.put("registrationLimit", registrationLimit);
        payload.put("imageId", imageId == null ? null : imageId.toString());
        data.put("data", payload);

        return functions
                .getHttpsCallable("createEvent")
                .call(data)
                .onSuccessTask(callResult -> {
                    Map<String, Object> result = (Map<String, Object>) callResult.getData();
                    return Tasks.forResult(UUID.fromString((String) result.get("eventId")));
                });
    }

    /**
     * Calls the getEvent cloud function, fetches an event from the database, and maps it to an Event model.
     *
     * @param eventId  UUID identify the event.
     * @param userId   UUID to identify user.
     * @param deviceId UUID to identify user's device.
     * @return Event object.
     */
    public Task<Event> getEvent(UUID eventId, UUID userId, UUID deviceId) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId.toString());
        data.put("deviceId", deviceId.toString());

        Map<String, Object> payload = new HashMap<>();
        payload.put("eventId", eventId.toString());
        data.put("data", payload);

        return functions
                .getHttpsCallable("getEvent")
                .call(data)
                .onSuccessTask(callResult -> {
                    Map<String, Object> eventData = (Map<String, Object>) callResult.getData();

                    return Tasks.forResult(new Event(eventId, eventData));
                });
    }

    /**
     * Calls the getEvents cloud function, filtering events for different fragments.
     *
     * @param userId    UUID to identify user.
     * @param deviceId  UUID to identify user's device.
     * @param max       Max number of events to return. Set to -1 for no maximum.
     * @param startFrom Event ID to start after (optional).
     * @param fetch     Filter mode (optional).
     * @param startDate ISO-8601 datetime with offset (optional).
     * @param endDate   ISO-8601 datetime with offset (optional).
     * @param search    Search string (optional).
     * @param sortBy    Sort mode.
     * @return List of Event objects after processing the search/filter query.
     */
    public Task<List<Event>> getEvents(UUID userId, UUID deviceId, int max,
                                       UUID startFrom, Fetch fetch,
                                       String startDate, String endDate,
                                       String search, SortBy sortBy) {
        Map<String, Object> data = new HashMap<>();

        data.put("userId", userId.toString());
        data.put("deviceId", deviceId.toString());

        // we represent no maximum by sending the largest possible integer value,
        // which is effectively no max
        if (max == -1) {
            max = Integer.MAX_VALUE;
        }

        if (fetch == null) {
            fetch = Fetch.AVAILABLE;
        }
        if (sortBy == null) {
            sortBy = SortBy.REGISTRATION_END;
        }

        Map<String, Object> filter = new HashMap<>();
        filter.put("fetch", fetch.getValue());
        filter.put("startDate", startDate);
        filter.put("endDate", endDate);
        filter.put("search", search);

        Map<String, Object> payload = new HashMap<>();
        payload.put("max", max);
        payload.put("startFrom", startFrom == null ? null : startFrom.toString());
        payload.put("filter", filter);
        payload.put("sortBy", sortBy.getValue());
        data.put("data", payload);

        Log.d("EventViewModel", "getEvents payload=" + data);

        return functions
                .getHttpsCallable("getEvents")
                .call(data)
                .onSuccessTask(callResult -> {
                    List<Map<String, Object>> result = (List<Map<String, Object>>) callResult.getData();
                    Log.d("EventViewModel", "getEvents result=" + result);
                    ArrayList<Event> events = new ArrayList<>();
                    if (result == null) {
                        return Tasks.forResult(events);
                    }
                    for (Map<String, Object> item : result) {
                        Event event = new Event(item);
                        if (event != null) {
                            events.add(event);
                        }
                    }
                    System.out.println("EVENTS ARRAY" + events);
                    return Tasks.forResult(events);
                });
    }

    /**
     * Calls the getOrganizerName cloud function and fetches the organizer name for an event.
     *
     * @param userId   UUID to identify user.
     * @param deviceId UUID to identify user's device.
     * @param eventId  UUID to identify the event.
     * @return String representing organizer name.
     */
    public Task<String> getOrganizerName(UUID userId, UUID deviceId, UUID eventId) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId.toString());
        data.put("deviceId", deviceId.toString());

        Map<String, Object> payload = new HashMap<>();
        payload.put("eventId", eventId.toString());
        data.put("data", payload);

        return functions
                .getHttpsCallable("getOrganizerName")
                .call(data)
                .onSuccessTask(callResult -> {
                        Map<String, Object> result = (Map<String, Object>) callResult.getData();
                        Object name = result == null ? null : result.get("name");
                        return Tasks.forResult(name == null ? null : name.toString());
                });
    }

    /**
     * Calls the getWaitlistCount cloud function to get the number of users on the waitlist of an event.
     *
     * @param userId   UUID to identify user.
     * @param deviceId UUID to identify user's device.
     * @param eventId  UUID to identify event.
     * @return Integer count of people on waitlist.
     */
    public Task<Integer> getWaitlistCount(UUID userId, UUID deviceId, UUID eventId) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId.toString());
        data.put("deviceId", deviceId.toString());

        Map<String, Object> payload = new HashMap<>();
        payload.put("eventId", eventId.toString());
        data.put("data", payload);

        return functions
                .getHttpsCallable("getWaitlistCount")
                .call(data)
                .onSuccessTask(callResult -> {
                        Map<String, Object> result = (Map<String, Object>) callResult.getData();
                        Object count = result == null ? null : result.get("count");
                        return Tasks.forResult(count instanceof Number ? ((Number) count).intValue() : 0);
                });
    }

    /**
     * Calls the checkWaitlist cloud function to check if a user is in waitlist of an event.
     *
     * @param userId   UUID to identify user.
     * @param deviceId UUID to identify user's device.
     * @param eventId  UUID to identify event.
     * @return Boolean value of true if user is in waitlist, false otherwise.
     */
    public Task<Boolean> checkWaitlist(UUID userId, UUID deviceId, UUID eventId) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId.toString());
        data.put("deviceId", deviceId.toString());

        Map<String, Object> payload = new HashMap<>();
        payload.put("eventId", eventId.toString());
        data.put("data", payload);

        Log.d("EventViewModel", "checkWaitlist payload type=" + data.getClass().getName());
        Log.d("EventViewModel", "checkWaitlist payload=" + data);

        return functions
                .getHttpsCallable("checkWaitlist")
                .call(data)
                .onSuccessTask(callResult -> {
                        Map<String, Object> result = (Map<String, Object>) callResult.getData();
                        Log.d("EventViewModel", "checkWaitlist result=" + result);
                        Object inWaitlist = result == null ? null : result.get("inWaitlist");
                        return Tasks.forResult(inWaitlist instanceof Boolean ? (Boolean) inWaitlist : false);
                });
    }

    /**
     * Calls the joinWaitlist cloud function to add a user to an event waitlist.
     *
     * @param userId   UUID to identify user.
     * @param deviceId UUID to identify user's device.
     * @param eventId  UUID to identify event.
     * @return null if successful, an error if unsuccessful.
     */
    public Task<Void> joinWaitlist(UUID userId, UUID deviceId, UUID eventId) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId.toString());
        data.put("deviceId", deviceId.toString());

        Map<String, Object> payload = new HashMap<>();
        payload.put("eventId", eventId.toString());
        data.put("data", payload);

        Log.d("EventViewModel", "joinWaitlist payload type=" + data.getClass().getName());
        Log.d("EventViewModel", "joinWaitlist payload=" + data);

        return FirebaseFunctions.getInstance()
                .getHttpsCallable("joinWaitlist")
                .call(data)
                .onSuccessTask(callResult -> {
                        Log.d("EventViewModel", "joinWaitlist success");
                        return Tasks.forResult(null);
                });
    }

    /**
     * Calls the leaveWaitlist cloud function to remove a user from an event waitlist.
     *
     * @param userId   UUID to identify user.
     * @param deviceId UUID to identify user's device.
     * @param eventId  UUID to identify event.
     * @return null if successful, an error if unsuccessful.
     */
    public Task<Void> leaveWaitlist(UUID userId, UUID deviceId, UUID eventId) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId.toString());
        data.put("deviceId", deviceId.toString());

        Map<String, Object> payload = new HashMap<>();
        payload.put("eventId", eventId.toString());
        data.put("data", payload);

        Log.d("EventViewModel", "leaveWaitlist payload type=" + data.getClass().getName());
        Log.d("EventViewModel", "leaveWaitlist payload=" + data);

        return functions
                .getHttpsCallable("leaveWaitlist")
                .call(data)
                .onSuccessTask(callResult -> {
                        Log.d("EventViewModel", "leaveWaitlist success");
                        return Tasks.forResult(null);
                });
    }

    /**
     * Calls the updateEvent cloud function to update an event.
     *
     * @param userId                UUID to identify user.
     * @param deviceId              UUID to identify user's device.
     * @param registrationStartTime ISO-8601 datetime with offset (UTC preferred).
     * @param registrationEndTime   ISO-8601 datetime with offset (UTC preferred).
     * @param eventTime             ISO-8601 datetime with offset (UTC preferred).
     * @param eventName             Name of the event.
     * @param eventDescription      Description of the event.
     * @param eventCategory         Category (optional).
     * @param eventGuidelines       Guidelines (optional).
     * @param geolocation           Whether geolocation is enabled.
     * @param eventCapacity         Entrant capacity.
     * @param location              Location of the event.
     * @param registrationLimit     Waitlist capacity (optional).
     * @param imageId               UUID identifying the image for the event (optional).
     * @return null if successful, an error if unsuccessful.
     */
    public Task<Void> updateEvent(UUID userId, UUID deviceId, UUID eventId,
                                  String registrationStartTime, String registrationEndTime, String eventTime,
                                  String eventName, String eventDescription,
                                  String eventCategory, String eventGuidelines,
                                  boolean geolocation, int eventCapacity,
                                  String location, Integer registrationLimit, UUID imageId) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId.toString());
        data.put("deviceId", deviceId.toString());

        Map<String, Object> payload = new HashMap<>();
        payload.put("eventId", eventId.toString());
        payload.put("registrationStartTime", registrationStartTime);
        payload.put("registrationEndTime", registrationEndTime);
        payload.put("eventTime", eventTime);
        payload.put("eventName", eventName);
        payload.put("eventDescription", eventDescription);
        payload.put("eventCategory", eventCategory);
        payload.put("eventGuidelines", eventGuidelines);
        payload.put("geolocation", geolocation);
        payload.put("eventCapacity", eventCapacity);
        payload.put("location", location);
        payload.put("registrationLimit", registrationLimit);
        payload.put("imageId", imageId == null ? null : imageId.toString());
        data.put("data", payload);

        return functions
                .getHttpsCallable("updateEvent")
                .call(data)
                .onSuccessTask(task -> Tasks.forResult(null));
    }

    /**
     * Calls the drawLottery cloud function to draw the lottery for an event.
     *
     * @param userId   UUID to identify user.
     * @param deviceId UUID to identify user's device.
     * @param eventId  UUID to identify event.
     * @return null on success, error on failure
     */
    public Task<Void> drawLottery(UUID userId, UUID deviceId, UUID eventId) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId.toString());
        data.put("deviceId", deviceId.toString());

        Map<String, Object> payload = new HashMap<>();
        payload.put("eventId", eventId.toString());
        data.put("data", payload);

        return functions.getHttpsCallable("drawLottery")
                .call(data)
                .onSuccessTask(task -> Tasks.forResult(null));
    }
}
