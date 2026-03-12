package ca.quanta.quantaevents.models;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Class which defines a Notification.
 */
public class Notification {
    private final UUID notificationId;
    private UUID senderId;
    private ArrayList<UUID> recipientIds;
    private String title;
    private String message;

    /**
     * Constructor for a Notification object.
     * @param senderId ID of a User who sent the notification.
     * @param recipientIds The IDs of users whom the notification was sent to.
     * @param message The notification's message.
     */
    public Notification(UUID senderId, ArrayList<UUID> recipientIds, String title, String message) {
        this.notificationId = UUID.randomUUID();
        this.senderId = senderId;
        this.recipientIds = recipientIds;
        this.message = message;
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }

    public String getMessage() {
        return this.message;
    }
}
