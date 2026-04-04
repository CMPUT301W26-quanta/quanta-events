package ca.quanta.quantaevents.models;

public class ExternalNotification {
    private final String title;
    private final String message;

    public ExternalNotification(String title, String message) {
        this.title = title;
        this.message = message;
    }

    public String getTitle() { return this.title; }
    public String getMessage() { return this.message; }
}
