package ca.quanta.quantaevents.models;

import java.util.UUID;

public class ExternalUndismissedNotification {
    private final UUID notificationId;
    private final UUID eventId;
    private final String title;
    private final String message;
    private final String kind;
    private final Boolean lotterySelected;

    public ExternalUndismissedNotification(UUID notificationId, UUID eventId, String title, String message, String kind, Boolean selected) {
        this.notificationId = notificationId;
        this.eventId = eventId;
        this.title = title;
        this.message = message;
        this.kind = kind;
        this.lotterySelected = selected;
    }

    public UUID getNotificationId() { return this.notificationId; }
    public UUID getEventId() { return this.eventId; }
    public String getTitle() { return this.title; }
    public String getMessage() { return this.message; }
    public String getKind() { return this.kind; }
    public Boolean getLotterySelected() { return this.lotterySelected; }
}
