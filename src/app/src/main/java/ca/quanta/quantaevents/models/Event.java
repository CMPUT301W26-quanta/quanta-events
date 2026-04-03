package ca.quanta.quantaevents.models;

import androidx.annotation.Nullable;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Class which defines an Event.
 */
public class Event {

    // Avoiding geolocation stuff for now
    private final UUID eventId;
    @Nullable
    private final UUID organizerId;

    private ArrayList<UUID> waitList;
    private ArrayList<UUID> cancelledList;
    private ArrayList<UUID> finalList;

    private ArrayList<UUID> commentsList;

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

    private final boolean drawn;

    private final boolean isPrivate;

    /**
     * Constructor for an Event object when loading from storage.
     */
    public Event(UUID eventId,
                 @Nullable UUID organizerId,
                 ArrayList<UUID> waitList,
                 ArrayList<UUID> cancelledList,
                 ArrayList<UUID> finalList,
                 ArrayList<UUID> commentsList,
                 ZonedDateTime registrationStartTime,
                 ZonedDateTime registrationEndTime,
                 @Nullable ZonedDateTime eventTime,
                 String eventName,
                 String eventDescription,
                 String location,
                 @Nullable String eventCategory,
                 @Nullable String eventGuidelines,
                 boolean geolocation,
                 @Nullable Integer eventCapacity,
                 @Nullable Integer registrationLimit,
                 @Nullable UUID imageId
    ) {
        this.eventId = eventId;
        this.organizerId = organizerId;
        this.waitList = waitList == null ? new ArrayList<>() : waitList;
        this.cancelledList = cancelledList == null ? new ArrayList<>() : cancelledList;
        this.finalList = finalList == null ? new ArrayList<>() : finalList;
        this.commentsList = commentsList == null ? new ArrayList<>() : commentsList;
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
        this.drawn = false;
        this.isPrivate = false;
    }

    public Event(UUID eventId, Map<String, Object> data){
        this.eventId = eventId;
        this.organizerId = data.get("organizer") == null ? null : UUID.fromString((String) data.get("organizer"));
        this.waitList = data.get("waitList") == null ? new ArrayList<>() : (ArrayList<UUID>) data.get("waitList");
        this.cancelledList = data.get("cancelledList") == null ? new ArrayList<>() : (ArrayList<UUID>) data.get("cancelledList");
        this.finalList = data.get("finalList") == null ? new ArrayList<>() : (ArrayList<UUID>) data.get("finalList");
        this.commentsList = data.get("commentsList") == null ? new ArrayList<>() : (ArrayList<UUID>) data.get("commentsList");
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
        this.drawn = Optional.ofNullable(data.get("drawn"))
                .flatMap(val -> val instanceof Boolean ? Optional.of((Boolean)val) : Optional.empty())
                .orElse(false);
        this.isPrivate = Optional.ofNullable(data.get("isPrivate"))
                .flatMap(val -> val instanceof Boolean ? Optional.of((Boolean)val) : Optional.empty())
                .orElse(false);
    }

    public Event(Map<String, Object> data) {
        this.eventId = UUID.fromString((String) data.get("eventId"));
        this.organizerId = data.get("organizer") == null ? null : UUID.fromString((String) data.get("organizer"));
        this.waitList = data.get("waitList") == null ? new ArrayList<>() : (ArrayList<UUID>) data.get("waitList");
        this.cancelledList = data.get("cancelledList") == null ? new ArrayList<>() : (ArrayList<UUID>) data.get("cancelledList");
        this.finalList = data.get("finalList") == null ? new ArrayList<>() : (ArrayList<UUID>) data.get("finalList");
        this.commentsList = data.get("commentsList") == null ? new ArrayList<>() : (ArrayList<UUID>) data.get("commentsList");
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
        this.drawn = Optional.ofNullable(data.get("drawn"))
                .flatMap(val -> val instanceof Boolean ? Optional.of((Boolean)val) : Optional.empty())
                .orElse(false);
        this.isPrivate = Optional.ofNullable(data.get("isPrivate"))
                .flatMap(val -> val instanceof Boolean ? Optional.of((Boolean)val) : Optional.empty())
                .orElse(false);
    }

    public UUID getEventId() {
        return eventId;
    }

    @Nullable
    public UUID getOrganizerId() {
        return organizerId;
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

    public ArrayList<UUID> getCommentsList(){
        return commentsList;
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

    public boolean isDrawn() {
        return drawn;
    }

    public boolean isPrivate() {
        return isPrivate;
    }
}
