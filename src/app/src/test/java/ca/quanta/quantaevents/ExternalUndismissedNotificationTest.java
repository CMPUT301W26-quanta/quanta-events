package ca.quanta.quantaevents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import ca.quanta.quantaevents.models.ExternalUndismissedNotification;

public class ExternalUndismissedNotificationTest {
    private static final UUID NOTIF_ID = UUID.randomUUID();
    private static final UUID EVENT_ID = UUID.randomUUID();
    private static final String TITLE = "Test Title";
    private static final String MESSAGE = "Test message";

    private static final String KIND = "INVITE";

    @Test
    public void ExternalUndismissedNotificationCreationTest() {
        ExternalUndismissedNotification notification = new ExternalUndismissedNotification(NOTIF_ID, EVENT_ID, TITLE, MESSAGE, KIND, false);
        assertEquals(NOTIF_ID, notification.getNotificationId());
        assertEquals(EVENT_ID, notification.getEventId());
        assertEquals(TITLE, notification.getTitle());
        assertEquals(MESSAGE, notification.getMessage());
        assertEquals(KIND, notification.getKind());
        assertNotNull(notification.getLotterySelected());
        assertEquals(false, notification.getLotterySelected());

        notification = new ExternalUndismissedNotification(NOTIF_ID, EVENT_ID, TITLE, MESSAGE, KIND, null);
        assertNull(notification.getLotterySelected());
    }
}
