package ca.quanta.quantaevents;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import ca.quanta.quantaevents.models.ExternalNotification;

public class ExternalNotificationTest {
    private static final String TITLE = "Test Title";
    private static final String MESSAGE = "Test message";

    @Test
    public void ExternalNotificationCreationTest() {
        ExternalNotification notification = new ExternalNotification(TITLE, MESSAGE);
        assertEquals(TITLE, notification.getTitle());
        assertEquals(MESSAGE, notification.getMessage());
    }
}
