package ca.quanta.quantaevents.adapters;

import android.graphics.Bitmap;

import androidx.annotation.Nullable;

import java.util.UUID;

public class EventCardItem {
    private final UUID eventId;
    private final String title;
    private final String time;
    private final String location;
    @Nullable
    private final Bitmap image;

    /**
     * Constructor for an EventCardItem.
     * @param eventId the event id of event
     * @param title TItle of the event
     * @param time Registration start time
     * @param location Event location
     * @param image Bitmap of the event image/poster if any uploaded
     */
    public EventCardItem(UUID eventId, String title, String time, String location, @Nullable Bitmap image) {
        this.eventId = eventId;
        this.title = title;
        this.time = time;
        this.location = location;
        this.image = image;
    }

    public UUID getEventId() {
        return eventId;
    }

    public String getTitle() {
        return title;
    }

    public String getTime() {
        return time;
    }

    public String getLocation() {
        return location;
    }

    @Nullable
    public Bitmap getImage() {
        return image;
    }

    public EventCardItem withImage(@Nullable Bitmap image) {
        return new EventCardItem(eventId, title, time, location, image);
    }
}
