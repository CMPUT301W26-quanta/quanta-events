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
    private String notificationMessage;

    /**
     * Constructor for a Notification object.
     * @param senderId ID of a User who sent the notification.
     * @param recipientIds The IDs of users whom the notification was sent to.
     * @param notificationMessage The notification's message.
     */
    Notification(UUID senderId, ArrayList<UUID> recipientIds, String notificationMessage) {
        this.notificationId = UUID.randomUUID();
        this.senderId = senderId;
        this.recipientIds = recipientIds;
        this.notificationMessage = notificationMessage;
    }

}