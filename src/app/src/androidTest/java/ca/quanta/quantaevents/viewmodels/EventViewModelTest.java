package ca.quanta.quantaevents.viewmodels;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.UUID;

public class EventViewModelTest {
    private static final String START = "2027-03-14T22:00:00.000Z";
    private static final String END = "2027-04-20T22:00:00.000Z";
    private static final String EVENT_TIME = "2027-05-26T22:00:00.000Z";
    private static final String NAME = "EventName";
    private static final String DESCRIPTION = "Event Description";
    private static final Double LOCATIONLAT = 53.5232723;
    private static final Double LOCATIONLNG = -113.5262886;
    private static final String CATEGORY = "Test Category";

    private static final String GUIDELINES = "Test Guidlines";
    private static final Integer REG_LIMIT = 100;

    private static final Integer CAPACITY = 10;

    private static final UUIDHolder userHolder = new UUIDHolder();

    private static final String USER_NAME = "INSTRUMENTED TEST USER";
    private static final String USER_EMAIL = "instrumented.user@gmail.com";
    private static final String USER_PHONE = "1234567890";
    private static final UUID USER_DEVICE_ID = new UUID(0, 0);

    EventViewModel events;

    public EventViewModelTest() {
        events = new EventViewModel();
    }

    @BeforeClass
    public static void createTestDependencies() throws InterruptedException {
        TaskHandler.handle(new UserViewModel().createUser(USER_NAME, USER_EMAIL, USER_PHONE, false, USER_DEVICE_ID), userHolder::setUuid);
    }

    @AfterClass
    public static void deleteTestDependencies() throws InterruptedException {
        TaskHandler.handle(new UserViewModel().deleteUser(userHolder.getUuid(), USER_DEVICE_ID, userHolder.getUuid()), _void -> {
        });

        // Deleting account should delete event
    }

    @Test
    public void MainEventTest() throws InterruptedException {
        UUIDHolder holder = new UUIDHolder();

        TaskHandler.handle(
                events.createEvent(
                        userHolder.getUuid(),
                        USER_DEVICE_ID,
                        START,
                        END,
                        EVENT_TIME,
                        NAME,
                        DESCRIPTION,
                        CATEGORY,
                        GUIDELINES,
                        false,
                        CAPACITY,
                        LOCATIONLAT,
                        LOCATIONLNG,
                        REG_LIMIT,
                        null,
                        true),
                holder::setUuid
        );

        TaskHandler.handle(
                events.getEvent(holder.getUuid(), userHolder.getUuid(), USER_DEVICE_ID),
                event -> {
                    assertEquals(holder.getUuid(), event.getEventId());
                    assertNotNull(event.getOrganizerId());
                    assertEquals(userHolder.getUuid(), event.getOrganizerId());
                    assertNotNull(event.getWaitList());
                    assertEquals(new ArrayList<>(), event.getWaitList());
                    assertNotNull(event.getCancelledList());
                    assertEquals(new ArrayList<>(), event.getCancelledList());
                    assertNotNull(event.getCommentsList());
                    assertEquals(new ArrayList<>(), event.getCommentsList());
                    assertNotNull(event.getFinalList());
                    assertEquals(new ArrayList<>(), event.getFinalList());
                    assertEquals(ZonedDateTime.parse(START), event.getRegistrationStartTime());
                    assertEquals(ZonedDateTime.parse(END), event.getRegistrationEndTime());
                    assertEquals(ZonedDateTime.parse(EVENT_TIME), event.getEventTime());
                    assertEquals(NAME, event.getEventName());
                    assertEquals(DESCRIPTION, event.getEventDescription());
                    assertEquals(LOCATIONLAT, event.getLocationLat());
                    assertEquals(LOCATIONLNG, event.getLocationLng());
                    assertEquals(CATEGORY, event.getEventCategory());
                    assertEquals(GUIDELINES, event.getEventGuidelines());
                    assertFalse(event.isGeolocationEnabled());
                    assertEquals(CAPACITY, event.getEventCapacity());
                    assertEquals(REG_LIMIT, event.getRegistrationLimit());
                    assertNull(event.getImageId());
                }
        );

        // Can't delete test event, we aren't admin
    }
}
