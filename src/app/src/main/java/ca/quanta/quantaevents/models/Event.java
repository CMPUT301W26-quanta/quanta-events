package ca.quanta.quantaevents.models;

import androidx.annotation.Nullable;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Class which defines an Event.
 */
public class Event {

    // Avoiding geolocation stuff for now
    private final UUID eventId;

    private ArrayList<UUID> waitList;
    private ArrayList<UUID> cancelledList;
    private ArrayList<UUID> finalList;

    private ZonedDateTime registrationStartTime;
    private ZonedDateTime registrationEndTime;

    private String eventName;
    private String eventDescription;
    private Float price;

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
     * @param price Cost to join the event.
     */
    public Event(ZonedDateTime registrationStartTime, ZonedDateTime registrationEndTime,
          String eventName, String eventDescription, Float price, Integer registrationLimit,
          UUID imageId) {
        this.eventId = UUID.randomUUID();
        this.waitList = new ArrayList<>();
        this.cancelledList = new ArrayList<>();
        this.finalList = new ArrayList<>();
        this.registrationStartTime = registrationStartTime;
        this.registrationEndTime = registrationEndTime;
        this.eventName = eventName;
        this.eventDescription = eventDescription;
        this.price = price;
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
     * @param price Cost to join the event.
     */
    public Event(ZonedDateTime registrationStartTime, ZonedDateTime registrationEndTime,
                 String eventName, String eventDescription, Float price, Integer registrationLimit) {
        this.eventId = UUID.randomUUID();
        this.waitList = new ArrayList<>();
        this.cancelledList = new ArrayList<>();
        this.finalList = new ArrayList<>();
        this.registrationStartTime = registrationStartTime;
        this.registrationEndTime = registrationEndTime;
        this.eventName = eventName;
        this.eventDescription = eventDescription;
        this.price = price;
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
     * @param price Cost to join the event.
     */
    public Event(ZonedDateTime registrationStartTime, ZonedDateTime registrationEndTime,
                 String eventName, String eventDescription, Float price, UUID imageId) {
        this.eventId = UUID.randomUUID();
        this.waitList = new ArrayList<>();
        this.cancelledList = new ArrayList<>();
        this.finalList = new ArrayList<>();
        this.registrationStartTime = registrationStartTime;
        this.registrationEndTime = registrationEndTime;
        this.eventName = eventName;
        this.eventDescription = eventDescription;
        this.price = price;
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
        this.waitList = new ArrayList<>();
        this.cancelledList = new ArrayList<>();
        this.finalList = new ArrayList<>();
        this.registrationStartTime = registrationStartTime;
        this.registrationEndTime = registrationEndTime;
        this.eventName = eventName;
        this.eventDescription = eventDescription;
        this.price = price;
        this.registrationLimit = null;
        this.imageId = null;
    }

}