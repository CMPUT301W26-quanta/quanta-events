package ca.quanta.quantaevents.adapters;

import android.graphics.Bitmap;

import androidx.annotation.Nullable;

import java.util.UUID;

/**
 * Card for displaying details related to events.
 */
public class EventCardItem {
    private final UUID eventId;
    private final String title;
    private final String registerStartTime;
    private final String location;
    @Nullable
    private final Bitmap image;

    /**
     * Constructor for an EventCardItem.
     * @param eventId the event id of event
     * @param title Title of the event
     * @param registerStartTime Registration start time
     * @param location Event location
     * @param image Bitmap of the event image/poster if any uploaded
     */
    public EventCardItem(UUID eventId, String title, String registerStartTime, String location, @Nullable Bitmap image) {
        this.eventId = eventId;
        this.title = title;
        this.registerStartTime = registerStartTime;
        this.location = location;
        this.image = image;
    }

    /**
     * Gets the UUID identifying an event.
     * @return UUID of the event.
     */
    public UUID getEventId() {
        return eventId;
    }

    /**
     * Gets the title of an event.
     * @return String representing title of event.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets the registration start time of an event.
     * @return String representing registration start time.
     */
    public String getRegistrationStartTime() {
        return registerStartTime;
    }

    /**
     * Gets the location of an event.
     * @return String representing location of an event.
     */
    public String getLocation() {
        return location;
    }

    /**
     * Gets the image data of an event.
     * @return Event's image data.
     */
    @Nullable
    public Bitmap getImage() {
        return image;
    }

    /**
     * Creates an event card using provided image data.
     * @param image Bitmap of image data.
     * @return Event card with new image data.
     */
    public EventCardItem withImage(@Nullable Bitmap image) {
        return new EventCardItem(eventId, title, registerStartTime, location, image);
    }

    /**
     * Creates an event card using provided location.
     * @param location String representing a location.
     * @return Event card with new location data.
     */
    public EventCardItem withLocation(String location) {
        return new EventCardItem(eventId, title, registerStartTime, location, image);
    }
}
