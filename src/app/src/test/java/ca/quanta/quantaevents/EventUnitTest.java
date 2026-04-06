package ca.quanta.quantaevents;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import ca.quanta.quantaevents.models.Event;

public class EventUnitTest {
    private static final UUID EVENT_ID = UUID.randomUUID();
    private static final UUID ORGANIZER_ID = UUID.randomUUID();
    private static final ZonedDateTime START = ZonedDateTime.of(2026, 3, 14, 12, 0, 0, 0, ZoneId.systemDefault());
    private static final ZonedDateTime END = ZonedDateTime.of(2026, 4, 20, 12, 0, 0, 0, ZoneId.systemDefault());
    private static final ZonedDateTime EVENT_TIME = ZonedDateTime.of(2026, 5, 26, 12, 0, 0, 0, ZoneId.systemDefault());
    private static final String NAME = "EventName";
    private static final String DESCRIPTION = "Event Description";
    private static final Double LOCATIONLAT = 53.5232723;
    private static final Double LOCATIONLNG = -113.5262886;
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
        ArrayList<UUID> commentsList = mockList();


        Event event = new Event(
                EVENT_ID,
                null,
                waitingList,
                cancelledList,
                finalList,
                commentsList,
                START,
                END,
                EVENT_TIME,
                NAME,
                DESCRIPTION,
                LOCATIONLAT,
                LOCATIONLNG,
                CATEGORY,
                GUIDELINES,
                true,
                CAPACITY,
                REG_LIMIT,
                IMAGE_ID);

        assertEquals(EVENT_ID, event.getEventId());
        assertNull(event.getOrganizerId());
        assertNotNull(event.getWaitList());
        assertEquals(waitingList, event.getWaitList());
        assertNotNull(event.getCancelledList());
        assertEquals(cancelledList, event.getCancelledList());
        assertNotNull(event.getCommentsList());
        assertEquals(commentsList, event.getCommentsList());
        assertNotNull(event.getFinalList());
        assertEquals(finalList, event.getFinalList());
        assertEquals(START, event.getRegistrationStartTime());
        assertEquals(END, event.getRegistrationEndTime());
        assertEquals(EVENT_TIME, event.getEventTime());
        assertEquals(NAME, event.getEventName());
        assertEquals(DESCRIPTION, event.getEventDescription());
        assertEquals(LOCATIONLAT, event.getLocationLat());
        assertEquals(LOCATIONLNG, event.getLocationLng());
        assertEquals(CATEGORY, event.getEventCategory());
        assertEquals(GUIDELINES, event.getEventGuidelines());
        assertTrue(event.isGeolocationEnabled());
        assertEquals(CAPACITY, event.getEventCapacity());
        assertEquals(REG_LIMIT, event.getRegistrationLimit());
        assertEquals(IMAGE_ID, event.getImageId());

        event = new Event(
                EVENT_ID,
                null,
                null,
                null,
                null,
                null,
                START,
                END,
                EVENT_TIME,
                NAME,
                DESCRIPTION,
                LOCATIONLAT,
                LOCATIONLNG,
                CATEGORY,
                GUIDELINES,
                false,
                CAPACITY,
                REG_LIMIT,
                IMAGE_ID
        );

        assertNotNull(event.getWaitList());
        assertEquals(0, event.getWaitList().size());
        assertNotNull(event.getCancelledList());
        assertEquals(0, event.getCancelledList().size());
        assertNotNull(event.getFinalList());
        assertEquals(0, event.getFinalList().size());
        assertNotNull(event.getFinalList());
        assertEquals(0, event.getCommentsList().size());
        assertFalse(event.isGeolocationEnabled());
    }

    @Test
    public void EventDataCreationTest() {
        ArrayList<UUID> waitingList = mockList();
        ArrayList<UUID> cancelledList = mockList();
        ArrayList<UUID> finalList = mockList();
        ArrayList<UUID> commentsList = mockList();

        Map<String, Object> data = new HashMap<>();
        data.put("organizer", ORGANIZER_ID.toString());
        data.put("waitList", waitingList);
        data.put("cancelledList", cancelledList);
        data.put("finalList", finalList);
        data.put("commentsList", commentsList);
        data.put("registrationStartTime", START.format(DateTimeFormatter.ISO_DATE_TIME));
        data.put("registrationEndTime", END.format(DateTimeFormatter.ISO_DATE_TIME));
        data.put("eventTime", EVENT_TIME.format(DateTimeFormatter.ISO_DATE_TIME));
        data.put("eventName", NAME);
        data.put("eventDescription", DESCRIPTION);
        data.put("locationLat", LOCATIONLAT);
        data.put("locationLng", LOCATIONLNG);
        data.put("eventCategory", CATEGORY);
        data.put("eventGuidelines", GUIDELINES);
        data.put("geolocation", true);
        data.put("eventCapacity", CAPACITY);
        data.put("registrationLimit", REG_LIMIT);
        data.put("imageId", IMAGE_ID);
        data.put("drawn", true);
        data.put("isPrivate", true);
        Event event = new Event(EVENT_ID, data);

        assertEquals(EVENT_ID, event.getEventId());
        assertNotNull(event.getOrganizerId());
        assertEquals(ORGANIZER_ID, event.getOrganizerId());
        assertNotNull(event.getWaitList());
        assertEquals(waitingList, event.getWaitList());
        assertNotNull(event.getCancelledList());
        assertEquals(cancelledList, event.getCancelledList());
        assertNotNull(event.getCommentsList());
        assertEquals(commentsList, event.getCommentsList());
        assertNotNull(event.getFinalList());
        assertEquals(finalList, event.getFinalList());
        assertEquals(START, event.getRegistrationStartTime());
        assertEquals(END, event.getRegistrationEndTime());
        assertEquals(EVENT_TIME, event.getEventTime());
        assertEquals(NAME, event.getEventName());
        assertEquals(DESCRIPTION, event.getEventDescription());
        assertEquals(LOCATIONLAT, event.getLocationLat());
        assertEquals(LOCATIONLNG, event.getLocationLng());
        assertEquals(CATEGORY, event.getEventCategory());
        assertEquals(GUIDELINES, event.getEventGuidelines());
        assertTrue(event.isGeolocationEnabled());
        assertEquals(CAPACITY, event.getEventCapacity());
        assertEquals(REG_LIMIT, event.getRegistrationLimit());
        assertEquals(IMAGE_ID, event.getImageId());
        assertTrue(event.isDrawn());
        assertTrue(event.isPrivate());

        data.put("eventId", EVENT_ID.toString());
        event = new Event(data);
        assertEquals(EVENT_ID, event.getEventId());
    }

    @Test
    public void EventNullDataCreationTest() {
        Map<String, Object> data = new HashMap<>();
        data.put("registrationStartTime", START.format(DateTimeFormatter.ISO_DATE_TIME));
        data.put("registrationEndTime", END.format(DateTimeFormatter.ISO_DATE_TIME));
        data.put("eventTime", EVENT_TIME.format(DateTimeFormatter.ISO_DATE_TIME));
        data.put("eventName", NAME);
        data.put("eventDescription", DESCRIPTION);

        Event event = new Event(EVENT_ID, data);

        assertEquals(EVENT_ID, event.getEventId());
        assertNull(event.getOrganizerId());
        assertNotNull(event.getWaitList());
        assertEquals(new ArrayList<>(), event.getWaitList());
        assertNotNull(event.getCancelledList());
        assertEquals(new ArrayList<>(), event.getCancelledList());
        assertNotNull(event.getCommentsList());
        assertEquals(new ArrayList<>(), event.getCommentsList());
        assertNotNull(event.getFinalList());
        assertEquals(new ArrayList<>(), event.getFinalList());
        assertEquals(START, event.getRegistrationStartTime());
        assertEquals(END, event.getRegistrationEndTime());
        assertEquals(EVENT_TIME, event.getEventTime());
        assertEquals(NAME, event.getEventName());
        assertEquals(DESCRIPTION, event.getEventDescription());
        assertNull(event.getLocationLat());
        assertNull(event.getLocationLng());
        assertNull(event.getEventCategory());
        assertNull(event.getEventGuidelines());
        assertFalse(event.isGeolocationEnabled());
        assertNotNull(event.getEventCapacity());
        assertEquals(0, event.getEventCapacity());
        assertNull(event.getRegistrationLimit());
        assertNull(event.getImageId());
        assertFalse(event.isDrawn());
        assertFalse(event.isPrivate());
    }
}
