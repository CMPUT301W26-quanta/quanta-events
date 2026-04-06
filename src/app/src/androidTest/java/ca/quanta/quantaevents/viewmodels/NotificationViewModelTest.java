package ca.quanta.quantaevents.viewmodels;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.UUID;

public class NotificationViewModelTest {
    private static final String EVENT_START = "2027-03-14T22:00:00.000Z";
    private static final String EVENT_END = "2027-04-20T22:00:00.000Z";
    private static final String EVENT_TIME = "2027-05-26T22:00:00.000Z";
    private static final String EVENT_NAME = "EventName";
    private static final String EVENT_DESCRIPTION = "Event Description";
    private static final Double EVENT_LOCATIONLAT = 53.5232723;
    private static final Double EVENT_LOCATIONLNG = -113.5262886;
    private static final String EVENT_CATEGORY = "Test Category";

    private static final String EVENT_GUIDELINES = "Test Guidlines";
    private static final Integer EVENT_REG_LIMIT = 100;

    private static final Integer EVENT_CAPACITY = 10;

    private static final UUIDHolder userHolder = new UUIDHolder();
    private static final UUIDHolder eventHolder = new UUIDHolder();

    private static final String USER_NAME = "INSTRUMENTED TEST USER";
    private static final String USER_EMAIL = "instrumented.user@gmail.com";
    private static final String USER_PHONE = "1234567890";
    private static final UUID USER_DEVICE_ID = new UUID(0, 0);

    private static final String TITLE = "Notification Title";
    private static final String DESCRIPTION = "Notification description";

    NotificationViewModel notifications;

    public NotificationViewModelTest() {
        notifications = new NotificationViewModel();
    }

    @BeforeClass
    public static void createTestDependencies() throws InterruptedException {
        TaskHandler.handle(new UserViewModel().createUser(USER_NAME, USER_EMAIL, USER_PHONE, false, USER_DEVICE_ID), userHolder::setUuid);

        TaskHandler.handle(
                new EventViewModel().createEvent(
                        userHolder.getUuid(),
                        USER_DEVICE_ID,
                        EVENT_START,
                        EVENT_END,
                        EVENT_TIME,
                        EVENT_NAME,
                        EVENT_DESCRIPTION,
                        EVENT_CATEGORY,
                        EVENT_GUIDELINES,
                        false,
                        EVENT_CAPACITY,
                        EVENT_LOCATIONLAT,
                        EVENT_LOCATIONLNG,
                        EVENT_REG_LIMIT,
                        null,
                        true),
                eventHolder::setUuid
        );
    }

    @AfterClass
    public static void deleteTestDependencies() throws InterruptedException {
        TaskHandler.handle(new UserViewModel().deleteUser(userHolder.getUuid(), USER_DEVICE_ID, userHolder.getUuid()), _void -> {
        });

        // Deleting account should delete event
    }

    @Test
    public void CreateNotificationTest() throws InterruptedException {
        TaskHandler.handle(
                notifications.createNotification(
                        userHolder.getUuid(),
                        USER_DEVICE_ID,
                        TITLE,
                        DESCRIPTION,
                        eventHolder.getUuid(),
                        true,
                        true,
                        true,
                        true
                ),
                _ignored -> {
                }
        );
    }
}
