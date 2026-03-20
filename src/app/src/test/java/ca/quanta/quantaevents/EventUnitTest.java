package ca.quanta.quantaevents;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.UUID;

import ca.quanta.quantaevents.models.Event;

public class EventUnitTest {
    private static final UUID EVENT_ID = UUID.randomUUID();
    private static final ZonedDateTime START = ZonedDateTime.of(2026, 3, 14, 12, 0, 0, 0, ZoneId.systemDefault());
    private static final ZonedDateTime END = ZonedDateTime.of(2026, 4, 20, 12, 0, 0, 0, ZoneId.systemDefault());
    private static final ZonedDateTime EVENT_TIME = ZonedDateTime.of(2026, 5, 26, 12, 0, 0, 0, ZoneId.systemDefault());
    private static final String NAME = "EventName";
    private static final String DESCRIPTION = "Event Description";
    private static final String LOCATION = "Here";
    private static final String CATEGORY = "Test Category";

    private static final String GUIDELINES = "Test Guidlines";
    private static final Integer REG_LIMIT = 10;

    private static final Integer CAPACITY = 100;
    private static final UUID IMAGE_ID = UUID.randomUUID();

    private ArrayList<UUID> mockList() {
        ArrayList<UUID> list = new ArrayList<>();
        list.add(UUID.randomUUID());
        return list;
    }

    @Test
    public void EventCreationTest() {

        ArrayList<UUID> waitingList = mockList();
        ArrayList<UUID> cancelledList = mockList();
        ArrayList<UUID> finalList = mockList();

        Event event = new Event(EVENT_ID, null, null,
                waitingList, cancelledList, finalList,
                START, END,
                EVENT_TIME, NAME, DESCRIPTION, LOCATION,
                CATEGORY, GUIDELINES, true, CAPACITY,
                REG_LIMIT, IMAGE_ID);

        assertEquals(EVENT_ID, event.getEventId());
        assertNull(event.getOrganizerId());
        assertNull(event.getOrganizerDeviceId());
        assertNotNull(event.getWaitList());
        assertEquals(waitingList, event.getWaitList());
        assertNotNull(event.getCancelledList());
        assertEquals(cancelledList, event.getCancelledList());
        assertNotNull(event.getFinalList());
        assertEquals(finalList, event.getFinalList());
        assertEquals(START, event.getRegistrationStartTime());
        assertEquals(END, event.getRegistrationEndTime());
        assertEquals(EVENT_TIME, event.getEventTime());
        assertEquals(DESCRIPTION, event.getEventDescription());
        assertEquals(LOCATION, event.getLocation());
        assertEquals(CATEGORY, event.getEventCategory());
        assertEquals(GUIDELINES, event.getEventGuidelines());
        assertTrue(event.isGeolocationEnabled());
        assertEquals(CAPACITY, event.getEventCapacity());
        assertEquals(REG_LIMIT, event.getRegistrationLimit());
        assertEquals(IMAGE_ID, event.getImageId());

        event = new Event(EVENT_ID, null, null,
                null, null, null,
                START, END,
                EVENT_TIME, NAME, DESCRIPTION, LOCATION,
                CATEGORY, GUIDELINES, false, CAPACITY,
                REG_LIMIT, IMAGE_ID);

        assertNotNull(event.getWaitList());
        assertEquals(0, event.getWaitList().size());
        assertNotNull(event.getCancelledList());
        assertEquals(0, event.getCancelledList().size());
        assertNotNull(event.getFinalList());
        assertEquals(0, event.getFinalList().size());
        assertFalse(event.isGeolocationEnabled());
    }
}
