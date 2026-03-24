package ca.quanta.quantaevents.models;

import androidx.annotation.Nullable;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

/**
 * Class which defines an Event.
 */
public class Event {

    // Avoiding geolocation stuff for now
    private final UUID eventId;
    @Nullable
    private final UUID organizerId;

    @Nullable
    private final UUID organizerDeviceId;

    private ArrayList<UUID> waitList;
    private ArrayList<UUID> cancelledList;
    private ArrayList<UUID> finalList;

    private ZonedDateTime registrationStartTime;
    private ZonedDateTime registrationEndTime;
    @Nullable
    private ZonedDateTime eventTime;

    private String eventName;
    private String eventDescription;
    private String location;
    @Nullable
    private String eventCategory;
    @Nullable
    private String eventGuidelines;
    private boolean geolocation;
    private Integer eventCapacity;

    @Nullable
    private Integer registrationLimit;
    @Nullable
    private UUID imageId;

    /**
     * Constructor for an Event object when all optional fields are filled.
     * @param imageId ID of an image object to be shown with the event.
     * @param registrationStartTime Date and time when registration for the event opens.
     * @param registrationEndTime Date and time when registration for the event closes.
     * @param registrationLimit Maximum number of entrants allowed to join the event.
     * @param eventName Name of the event.
     * @param eventDescription Description of the event.
     */
    public Event(ZonedDateTime registrationStartTime, ZonedDateTime registrationEndTime,
                 String eventName, String eventDescription, Float price, Integer registrationLimit,
                 UUID imageId) {
        this.eventId = UUID.randomUUID();
        this.organizerId = null;
        this.organizerDeviceId = null;
        this.waitList = new ArrayList<>();
        this.cancelledList = new ArrayList<>();
        this.finalList = new ArrayList<>();
        this.registrationStartTime = registrationStartTime;
        this.registrationEndTime = registrationEndTime;
        this.eventTime = null;
        this.eventName = eventName;
        this.eventDescription = eventDescription;
        this.location = "";
        this.eventCategory = null;
        this.eventGuidelines = null;
        this.geolocation = false;
        this.eventCapacity = 0;
        this.registrationLimit = registrationLimit;
        this.imageId = imageId;
    }

    /**
     * Constructor for an Event object with no image.
     * @param registrationStartTime Date and time when registration for the event opens.
     * @param registrationEndTime Date and time when registration for the event closes.
     * @param registrationLimit Maximum number of entrants allowed to join the event.
     * @param eventName Name of the event.
     * @param eventDescription Description of the event.
     */
    public Event(ZonedDateTime registrationStartTime, ZonedDateTime registrationEndTime,
                 String eventName, String eventDescription, Float price, Integer registrationLimit) {
        this.eventId = UUID.randomUUID();
        this.organizerId = null;
        this.organizerDeviceId = null;
        this.waitList = new ArrayList<>();
        this.cancelledList = new ArrayList<>();
        this.finalList = new ArrayList<>();
        this.registrationStartTime = registrationStartTime;
        this.registrationEndTime = registrationEndTime;
        this.eventTime = null;
        this.eventName = eventName;
        this.eventDescription = eventDescription;
        this.location = "";
        this.eventCategory = null;
        this.eventGuidelines = null;
        this.geolocation = false;
        this.eventCapacity = 0;
        this.registrationLimit = registrationLimit;
        this.imageId = null;
    }

    /**
     * Constructor for an Event object with no registration capacity.
     * @param imageId ID of an image object to be shown with the event.
     * @param registrationStartTime Date and time when registration for the event opens.
     * @param registrationEndTime Date and time when registration for the event closes.
     * @param eventName Name of the event.
     * @param eventDescription Description of the event.
     */
    public Event(ZonedDateTime registrationStartTime, ZonedDateTime registrationEndTime,
                 String eventName, String eventDescription, Float price, UUID imageId) {
        this.eventId = UUID.randomUUID();
        this.organizerId = null;
        this.organizerDeviceId = null;
        this.waitList = new ArrayList<>();
        this.cancelledList = new ArrayList<>();
        this.finalList = new ArrayList<>();
        this.registrationStartTime = registrationStartTime;
        this.registrationEndTime = registrationEndTime;
        this.eventTime = null;
        this.eventName = eventName;
        this.eventDescription = eventDescription;
        this.location = "";
        this.eventCategory = null;
        this.eventGuidelines = null;
        this.geolocation = false;
        this.eventCapacity = 0;
        this.registrationLimit = null;
        this.imageId = imageId;
    }

    /**
     * Constructor for an Event when no optional fields are filled.
     * @param registrationStartTime Date and time when registration for the event opens.
     * @param registrationEndTime Date and time when registration for the event closes.
     * @param eventName Name of the event.
     * @param eventDescription Description of the event.
     * @param price Cost to join the event.
     */
    public Event(ZonedDateTime registrationStartTime, ZonedDateTime registrationEndTime,
                 String eventName, String eventDescription, Float price) {
        this.eventId = UUID.randomUUID();
        this.organizerId = null;
        this.organizerDeviceId = null;
        this.waitList = new ArrayList<>();
        this.cancelledList = new ArrayList<>();
        this.finalList = new ArrayList<>();
        this.registrationStartTime = registrationStartTime;
        this.registrationEndTime = registrationEndTime;
        this.eventTime = null;
        this.eventName = eventName;
        this.eventDescription = eventDescription;
        this.location = "";
        this.eventCategory = null;
        this.eventGuidelines = null;
        this.geolocation = false;
        this.eventCapacity = 0;
        this.registrationLimit = null;
        this.imageId = null;
    }

    /**
     * Constructor for an Event object when loading from storage.
     */
    public Event(UUID eventId, @Nullable UUID organizerId, @Nullable UUID organizerDeviceId,
                 ArrayList<UUID> waitList, ArrayList<UUID> cancelledList, ArrayList<UUID> finalList,
                 ZonedDateTime registrationStartTime, ZonedDateTime registrationEndTime,
                 @Nullable ZonedDateTime eventTime, String eventName, String eventDescription,
                 String location, @Nullable String eventCategory, @Nullable String eventGuidelines,
                 boolean geolocation, @Nullable Integer eventCapacity,
                 @Nullable Integer registrationLimit, @Nullable UUID imageId) {
        this.eventId = eventId;
        this.organizerId = organizerId;
        this.organizerDeviceId = organizerDeviceId;
        this.waitList = waitList == null ? new ArrayList<>() : waitList;
        this.cancelledList = cancelledList == null ? new ArrayList<>() : cancelledList;
        this.finalList = finalList == null ? new ArrayList<>() : finalList;
        this.registrationStartTime = registrationStartTime;
        this.registrationEndTime = registrationEndTime;
        this.eventTime = eventTime;
        this.eventName = eventName;
        this.eventDescription = eventDescription;
        this.location = location;
        this.eventCategory = eventCategory;
        this.eventGuidelines = eventGuidelines;
        this.geolocation = geolocation;
        this.eventCapacity = eventCapacity == null ? 0 : eventCapacity;
        this.registrationLimit = registrationLimit;
        this.imageId = imageId;
    }

    public Event(UUID eventId, Map<String, Object> data){
        this.eventId = eventId;
        this.organizerId = data.get("organizer") == null ? null : UUID.fromString((String) data.get("organizer"));
        this.organizerDeviceId = data.get("organizerDeviceId") == null ? null : UUID.fromString(data.get("organizerDeviceId").toString());
        this.waitList = data.get("waitList") == null ? new ArrayList<>() : (ArrayList<UUID>) data.get("waitList");
        this.cancelledList = data.get("cancelledList") == null ? new ArrayList<>() : (ArrayList<UUID>) data.get("cancelledList");
        this.finalList = data.get("finalList") == null ? new ArrayList<>() : (ArrayList<UUID>) data.get("finalList");
        this.registrationStartTime = ZonedDateTime.parse(data.get("registrationStartTime").toString());
        this.registrationEndTime = ZonedDateTime.parse(data.get("registrationEndTime").toString());
        this.eventTime = data.get("eventTime") == null ? null : ZonedDateTime.parse(data.get("eventTime").toString());
        this.eventName = data.get("eventName").toString();
        this.eventDescription = data.get("eventDescription").toString();
        this.location = data.get("location").toString();
        this.eventCategory = data.get("eventCategory") == null ? null : data.get("eventCategory").toString();
        this.eventGuidelines = data.get("eventGuidelines") == null ? null : data.get("eventGuidelines").toString();
        this.geolocation = Boolean.parseBoolean(data.getOrDefault("geolocation", "false").toString());
        this.eventCapacity = Integer.parseInt(data.getOrDefault("eventCapacity", "0").toString());
        this.registrationLimit = data.get("registrationLimit") == null ? null : Integer.parseInt(data.get("registrationLimit").toString());
        this.imageId = data.get("imageId") == null ? null : UUID.fromString(data.get("imageId").toString());
    }

    public Event(Map<String, Object> data) {
        this.eventId = UUID.fromString((String) data.get("eventId"));
        this.organizerId = data.get("organizer") == null ? null : UUID.fromString((String) data.get("organizer"));
        this.organizerDeviceId = data.get("organizerDeviceId") == null ? null : UUID.fromString(data.get("organizerDeviceId").toString());
        this.waitList = data.get("waitList") == null ? new ArrayList<>() : (ArrayList<UUID>) data.get("waitList");
        this.cancelledList = data.get("cancelledList") == null ? new ArrayList<>() : (ArrayList<UUID>) data.get("cancelledList");
        this.finalList = data.get("finalList") == null ? new ArrayList<>() : (ArrayList<UUID>) data.get("finalList");
        this.registrationStartTime = ZonedDateTime.parse(data.get("registrationStartTime").toString());
        this.registrationEndTime = ZonedDateTime.parse(data.get("registrationEndTime").toString());
        this.eventTime = data.get("eventTime") == null ? null : ZonedDateTime.parse(data.get("eventTime").toString());
        this.eventName = data.get("eventName").toString();
        this.eventDescription = data.get("eventDescription").toString();
        this.location = data.get("location").toString();
        this.eventCategory = data.get("eventCategory") == null ? null : data.get("eventCategory").toString();
        this.eventGuidelines = data.get("eventGuidelines") == null ? null : data.get("eventGuidelines").toString();
        this.geolocation = Boolean.parseBoolean(data.getOrDefault("geolocation", "false").toString());
        this.eventCapacity = Integer.parseInt(data.getOrDefault("eventCapacity", "0").toString());
        this.registrationLimit = data.get("registrationLimit") == null ? null : Integer.parseInt(data.get("registrationLimit").toString());
        this.imageId = data.get("imageId") == null ? null : UUID.fromString(data.get("imageId").toString());
    }

    public UUID getEventId() {
        return eventId;
    }

    @Nullable
    public UUID getOrganizerId() {
        return organizerId;
    }

    @Nullable
    public UUID getOrganizerDeviceId() {
        return organizerDeviceId;
    }

    public ArrayList<UUID> getWaitList() {
        return waitList;
    }

    public ArrayList<UUID> getCancelledList() {
        return cancelledList;
    }

    public ArrayList<UUID> getFinalList() {
        return finalList;
    }

    public ZonedDateTime getRegistrationStartTime() {
        return registrationStartTime;
    }

    public ZonedDateTime getRegistrationEndTime() {
        return registrationEndTime;
    }

    @Nullable
    public ZonedDateTime getEventTime() {
        return eventTime;
    }

    public String getEventName() {
        return eventName;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public String getLocation() {
        return location;
    }

    @Nullable
    public String getEventCategory() {
        return eventCategory;
    }

    @Nullable
    public String getEventGuidelines() {
        return eventGuidelines;
    }

    public boolean isGeolocationEnabled() {
        return geolocation;
    }

    public Integer getEventCapacity() {
        return eventCapacity;
    }

    @Nullable
    public Integer getRegistrationLimit() {
        return registrationLimit;
    }

    @Nullable
    public UUID getImageId() {
        return imageId;
    }

}
