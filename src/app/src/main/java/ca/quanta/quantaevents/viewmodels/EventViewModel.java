package ca.quanta.quantaevents.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import ca.quanta.quantaevents.models.Event;

/**
 * Class which represents an EventViewModel object.
 */
public class EventViewModel extends ViewModel {

    // Initialize an instance of cloud functions
    private FirebaseFunctions functions = FirebaseFunctions.getInstance();

    /**
     * Calls the createEvent cloud function, adding an event to the database.
     * @param userId UUID identifying the organizer user.
     * @param deviceId UUID identifying the user's device.
     * @param registrationStartTime ISO-8601 datetime with offset (UTC preferred).
     * @param registrationEndTime ISO-8601 datetime with offset (UTC preferred).
     * @param eventName Name of the event.
     * @param eventDescription Description of the event.
     * @param location Location of the event.
     * @param registrationLimit Maximum number of entrants allowed to join the event (optional).
     * @param imageId UUID identifying the image for the event (optional).
     * @return UUID identifying the event's ID.
     */
    public Task<String> createEvent(UUID userId, UUID deviceId, String registrationStartTime,
                                    String registrationEndTime, String eventName,
                                    String eventDescription, String location,
                                    Integer registrationLimit, UUID imageId) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId.toString());
        data.put("deviceId", deviceId.toString());

        Map<String, Object> payload = new HashMap<>();
        payload.put("registrationStartTime", registrationStartTime);
        payload.put("registrationEndTime", registrationEndTime);
        payload.put("eventName", eventName);
        payload.put("eventDescription", eventDescription);
        payload.put("location", location);
        if (registrationLimit != null) {
            payload.put("registrationLimit", registrationLimit);
        }
        if (imageId != null) {
            payload.put("imageId", imageId.toString());
        }
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

        String eventName = valueToString(data.get("eventName"));
        String eventDescription = valueToString(data.get("eventDescription"));
        String location = valueToString(data.get("location"));
        Integer registrationLimit = parseInteger(data.get("registrationLimit"));
        UUID imageId = parseUUID(valueToString(data.get("imageId")));

        return new Event(eventId, organizerId, waitList, cancelledList, finalList,
                start, end, eventName, eventDescription, location, registrationLimit, imageId);
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
        if (!(value instanceof java.util.List)) {
            return result;
        }
        for (Object item : (java.util.List<Object>) value) {
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
