package ca.quanta.quantaevents.models;

/**
 * Class which defines an external notification, a simpler notification.
 */
public class ExternalNotification {
    private final String title;
    private final String message;

    /**
     * Constructor for an external notification.
     * @param title Title of the notification.
     * @param message Body of the notification.
     */
    public ExternalNotification(String title, String message) {
        this.title = title;
        this.message = message;
    }

    /**
     * Gets the title of the notifiction.
     * @return Title of the notification.
     */
    public String getTitle() { return this.title; }

    /**
     * Gets the body message of the notification.
     * @return Notification message.
     */
    public String getMessage() { return this.message; }
}
