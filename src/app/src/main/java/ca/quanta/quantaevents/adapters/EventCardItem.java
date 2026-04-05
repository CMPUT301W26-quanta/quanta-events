package ca.quanta.quantaevents.adapters;

import android.graphics.Bitmap;

import androidx.annotation.Nullable;

import java.util.UUID;

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
     * @param title TItle of the event
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

    public UUID getEventId() {
        return eventId;
    }

    public String getTitle() {
        return title;
    }

    public String getRegistrationStartTime() {
        return registerStartTime;
    }

    public String getLocation() {
        return location;
    }

    @Nullable
    public Bitmap getImage() {
        return image;
    }

    public EventCardItem withImage(@Nullable Bitmap image) {
        return new EventCardItem(eventId, title, registerStartTime, location, image);
    }
    public EventCardItem withLocation(String location) {
        return new EventCardItem(eventId, title, registerStartTime, location, image);
    }
}
