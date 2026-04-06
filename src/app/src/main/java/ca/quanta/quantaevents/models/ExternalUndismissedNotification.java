package ca.quanta.quantaevents.models;

import java.util.UUID;

/**
 * Class that defines an external undismissed notification, a simpler undismissed notification.
 */
public class ExternalUndismissedNotification {
    private final UUID notificationId;
    private final UUID eventId;
    private final String title;
    private final String message;
    private final String kind;
    private final Boolean lotterySelected;

    /**
     * Constructor for an external undismissed notification.
     * @param notificationId UUID identifying the notification.
     * @param eventId UUID identifying the event the notif is for.
     * @param title Title of the notification.
     * @param message Body message of the notification.
     * @param kind Type of notification.
     * @param selected Boolean that's true when the user receiving this was selected for an event.
     */
    public ExternalUndismissedNotification(UUID notificationId, UUID eventId, String title, String message, String kind, Boolean selected) {
        this.notificationId = notificationId;
        this.eventId = eventId;
        this.title = title;
        this.message = message;
        this.kind = kind;
        this.lotterySelected = selected;
    }

    /**
     * Gets the notification's ID.
     * @return UUID identifying notification.
     */
    public UUID getNotificationId() { return this.notificationId; }

    /**
     * Gets the notification's event's ID.
     * @return UUID identifying the event.
     */
    public UUID getEventId() { return this.eventId; }

    /**
     * Gets the title of the notification.
     * @return Title of the notification.
     */
    public String getTitle() { return this.title; }

    /**
     * Gets the message of the notification.
     * @return Message of the notification.
     */
    public String getMessage() { return this.message; }

    /**
     * Gets the notification type.
     * @return Type of notification.
     */
    public String getKind() { return this.kind; }

    /**
     * Gets if the user was selected for an event.
     * @return True if they were, False otherwise.
     */
    public Boolean getLotterySelected() { return this.lotterySelected; }
}
