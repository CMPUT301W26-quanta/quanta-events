package ca.quanta.quantaevents.models;

import java.util.UUID;

/**
 * Class which defines a Notification.
 */
public class Notification {
    private UUID eventId;
    private String title;
    private String message;
    private Boolean waited;
    private Boolean selected;
    private Boolean cancelled;

    /**
     * Constructor for a Notification object.
     * @param eventId The id of the event this notification is associated with.
     * @param title The notification's title.
     * @param message The notification's message.
     * @param waited If sent to waitlisted users.
     * @param selected If sent to selected users.
     * @param cancelled If sent to cancelled users.
     */
    public Notification(UUID eventId, String title, String message, Boolean waited, Boolean selected, Boolean cancelled) {
        this.eventId = eventId;
        this.title = title;
        this.message = message;
        this.waited = waited;
        this.selected = selected;
        this.cancelled = cancelled;
    }

    public UUID getEventId() {
        return this.eventId;
    }

    public String getTitle() {
        return this.title;
    }

    public String getMessage() {
        return this.message;
    }

    public Boolean getSentToWaitlisted() {
        return this.waited;
    }

    public Boolean getSentToSelected() {
        return this.selected;
    }

    public Boolean getSentToCancelled() {
        return this.cancelled;
    }
}
