package ca.quanta.quantaevents.viewmodels;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import ca.quanta.quantaevents.models.Event;

/**
 * Class which represents an EventViewModel object.
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
     * Calls the createEvent cloud function, adding an event to the database.
     * @param userId UUID identifying the organizer user.
     * @param deviceId UUID identifying the user's device.
     * @param registrationStartTime ISO-8601 datetime with offset (UTC preferred).
     * @param registrationEndTime ISO-8601 datetime with offset (UTC preferred).
     * @param eventTime ISO-8601 datetime with offset (UTC preferred).
     * @param eventName Name of the event.
     * @param eventDescription Description of the event.
     * @param eventCategory Category (optional).
     * @param eventGuidelines Guidelines (optional).
     * @param geolocation Whether geolocation is enabled.
     * @param eventCapacity Entrant capacity.
     * @param location Location of the event.
     * @param registrationLimit Waitlist capacity (optional).
     * @param imageId UUID identifying the image for the event (optional).
     * @return UUID identifying the event's ID.
     */
    public Task<String> createEvent(UUID userId, UUID deviceId, String registrationStartTime,
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
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        Map<String, Object> result = (Map<String, Object>) task.getResult().getData();
                        return (String) result.get("eventId");
                    }
                });
    }

    /**
     * Calls the getEvent cloud function, fetching an event from the database and mapping it to an Event model.
     * @param eventId UUID identifying the event.
     * @param userId UUID identifying the user.
     * @param deviceId UUID identifying the user's device.
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
                .continueWith(new Continuation<HttpsCallableResult, Event>() {
                    @Override
                    public Event then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        Map<String, Object> eventData = (Map<String, Object>) task.getResult().getData();
                        return mapToEvent(eventId, eventData);
                    }
                });
    }

    /**
     * Calls the getEvents cloud function, returning a list of Event models.
     * @param userId UUID identifying the user.
     * @param deviceId UUID identifying the user's device.
     * @param max Max number of events to return.
     * @param startFrom Event ID to start after (optional).
     * @param fetch Filter mode.
     * @param startDate ISO-8601 datetime with offset (optional).
     * @param endDate ISO-8601 datetime with offset (optional).
     * @param search Search string (optional).
     * @param sortBy Sort mode.
     * @return List of Event objects.
     */
    public Task<List<Event>> getEvents(UUID userId, UUID deviceId, int max,
                                       UUID startFrom, Fetch fetch,
                                       String startDate, String endDate,
                                       String search, SortBy sortBy) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId.toString());
        data.put("deviceId", deviceId.toString());

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
                .continueWith(new Continuation<HttpsCallableResult, List<Event>>() {
                    @Override
                    public List<Event> then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        List<Map<String, Object>> result = (List<Map<String, Object>>) task.getResult().getData();
                        Log.d("EventViewModel", "getEvents result=" + result);
                        ArrayList<Event> events = new ArrayList<>();
                        if (result == null) {
                            return events;
                        }
                        for (Map<String, Object> item : result) {
                            Event event = mapToEvent(item);
                            if (event != null) {
                                events.add(event);
                            }
                        }
                        return events;
                    }
                });
    }

    /**
     * Calls the getOrganizerName cloud function for an event.
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
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) {
                        Map<String, Object> result = (Map<String, Object>) task.getResult().getData();
                        Object name = result == null ? null : result.get("name");
                        return name == null ? null : name.toString();
                    }
                });
    }

    /**
     * Calls the getWaitlistCount cloud function for an event.
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
                .continueWith(new Continuation<HttpsCallableResult, Integer>() {
                    @Override
                    public Integer then(@NonNull Task<HttpsCallableResult> task) {
                        Map<String, Object> result = (Map<String, Object>) task.getResult().getData();
                        Object count = result == null ? null : result.get("count");
                        return count instanceof Number ? ((Number) count).intValue() : 0;
                    }
                });
    }

    /**
     * Calls the inWaitlist cloud function for an event.
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
                .continueWith(new Continuation<HttpsCallableResult, Boolean>() {
                    @Override
                    public Boolean then(@NonNull Task<HttpsCallableResult> task) {
                        Map<String, Object> result = (Map<String, Object>) task.getResult().getData();
                        Log.d("EventViewModel", "checkWaitlist result=" + result);
                        Object inWaitlist = result == null ? null : result.get("inWaitlist");
                        return inWaitlist instanceof Boolean ? (Boolean) inWaitlist : false;
                    }
                });
    }

    /**
     * Calls the joinWaitlist cloud function for an event.
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
                .continueWith(new Continuation<HttpsCallableResult, Void>() {
                    @Override
                    public Void then(@NonNull Task<HttpsCallableResult> task) {
                        Log.d("EventViewModel", "joinWaitlist success");
                        return null;
                    }
                });
    }

    /**
     * Calls the leaveWaitlist cloud function for an event.
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
                .continueWith(new Continuation<HttpsCallableResult, Void>() {
                    @Override
                    public Void then(@NonNull Task<HttpsCallableResult> task) {
                        Log.d("EventViewModel", "leaveWaitlist success");
                        return null;
                    }
                });
    }

    /**
     * Calls the updateEvent cloud function.
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
                .continueWith(new Continuation<HttpsCallableResult, Void>() {
                    @Override
                    public Void then(@NonNull Task<HttpsCallableResult> task) {
                        return null;
                    }
                });
    }


    @SuppressWarnings("unchecked")
    private static Event mapToEvent(UUID eventId, Map<String, Object> data) {
        if (data == null) {
            return null;
        }
        UUID organizerId = parseUUID(valueToString(data.get("organizer")));
        ArrayList<UUID> waitList = parseUuidList(data.get("waitList"));
        ArrayList<UUID> cancelledList = parseUuidList(data.get("cancelledList"));
        ArrayList<UUID> finalList = parseUuidList(data.get("finalList"));

        ZonedDateTime start = parseZonedDateTime(valueToString(data.get("registrationStartTime")));
        ZonedDateTime end = parseZonedDateTime(valueToString(data.get("registrationEndTime")));
        ZonedDateTime eventTime = parseZonedDateTime(valueToString(data.get("eventTime")));

        String eventName = valueToString(data.get("eventName"));
        String eventDescription = valueToString(data.get("eventDescription"));
        String location = valueToString(data.get("location"));
        String eventCategory = valueToString(data.get("eventCategory"));
        String eventGuidelines = valueToString(data.get("eventGuidelines"));
        boolean geolocation = Boolean.TRUE.equals(data.get("geolocation"));
        Integer eventCapacity = parseInteger(data.get("eventCapacity"));
        Integer registrationLimit = parseInteger(data.get("registrationLimit"));
        UUID imageId = parseUUID(valueToString(data.get("imageId")));

        return new Event(eventId, organizerId, waitList, cancelledList, finalList,
                start, end, eventTime, eventName, eventDescription, location,
                eventCategory, eventGuidelines, geolocation, eventCapacity,
                registrationLimit, imageId);
    }

    private static Event mapToEvent(Map<String, Object> data) {
        if (data == null) {
            return null;
        }
        UUID eventId = parseUUID(valueToString(data.get("eventId")));
        if (eventId == null) {
            return null;
        }
        return mapToEvent(eventId, data);
    }

    private static String valueToString(Object value) {
        if (value == null) {
            return null;
        }
        String result = value.toString().trim();
        return result.isEmpty() ? null : result;
    }

    private static ZonedDateTime parseZonedDateTime(String value) {
        if (value == null) {
            return null;
        }
        try {
            return ZonedDateTime.parse(value);
        } catch (Exception ex) {
            return null;
        }
    }

    private static Integer parseInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static ArrayList<UUID> parseUuidList(Object value) {
        ArrayList<UUID> result = new ArrayList<>();
        if (!(value instanceof List)) {
            return result;
        }
        for (Object item : (List<Object>) value) {
            UUID id = parseUUID(valueToString(item));
            if (id != null) {
                result.add(id);
            }
        }
        return result;
    }

    private static UUID parseUUID(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
