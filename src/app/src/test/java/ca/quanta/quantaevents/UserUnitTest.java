package ca.quanta.quantaevents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.UUID;

import ca.quanta.quantaevents.models.User;


public class UserUnitTest {
    private static final String NAME = "TestUser";
    private static final String EMAIL = "test.user@gmail.com";
    private static final String PHONE = "1234567890";

    @Test
    public void UserBasicCreationTest() {
        User user = new User(NAME, EMAIL, PHONE, true, true, true, true);

        assertEquals(NAME, user.getName(), "Failed with name");
        assertEquals(EMAIL, user.getEmail(), "Failed with email");
        assertEquals(PHONE, user.getPhoneNumber(), "Failed with phone");
        assertTrue(user.isEntrant());
        assertTrue(user.isOrganizer());
        assertTrue(user.isAdmin());
        assertNotNull(user.getEntrant());
        assertTrue(user.getEntrant().getReceiveNotifications());

        user = new User(NAME, EMAIL, PHONE, true, false, true, true);

        assertFalse(user.isEntrant());
        assertTrue(user.isOrganizer());
        assertTrue(user.isAdmin());
        assertNull(user.getEntrant());

        user = new User(NAME, EMAIL, PHONE, true, true, false, true);

        assertTrue(user.isEntrant());
        assertFalse(user.isOrganizer());
        assertTrue(user.isAdmin());
        assertNotNull(user.getEntrant());

        user = new User(NAME, EMAIL, PHONE, true, true, true, false);

        assertTrue(user.isEntrant());
        assertTrue(user.isOrganizer());
        assertFalse(user.isAdmin());
        assertNotNull(user.getEntrant());
    }

    @Test
    public void UserServerCreationTest() {
        UUID userId = UUID.randomUUID();
        UUID deviceId = UUID.randomUUID();

        User user = new User(null, null, null, false, false, false, false, userId, deviceId);
        HashMap<String, Object> data = new HashMap<>();

        assertEquals(user, new User(data, userId, deviceId), "Failed with blank data");

        data.put("unrelated", 1234);

        assertEquals(user, new User(data, userId, deviceId), "Failed with unrelated data");

        data.put("name", NAME);
        data.put("email", EMAIL);
        data.put("phone", PHONE);

        user = new User(NAME, EMAIL, PHONE, false, false, false, false, userId, deviceId);

        assertEquals(user, new User(data, userId, deviceId), "Failed with simple data");

        HashMap<String, Object> entrant = new HashMap<>();
        entrant.put("receiveNotifications", false);
        data.put("entrant", entrant);

        user = new User(NAME, EMAIL, PHONE, false, true, false, false, userId, deviceId);

        assertEquals(user, new User(data, userId, deviceId), "Failed with entrant, no notifications");

        entrant.put("receiveNotifications", true);

        user = new User(NAME, EMAIL, PHONE, true, true, false, false, userId, deviceId);

        assertEquals(user, new User(data, userId, deviceId), "Failed with entrant, yes notifications");

        data.put("organizer", new HashMap<>());
        data.put("admin", new HashMap<>());

        user = new User(NAME, EMAIL, PHONE, true, true, true, true, userId, deviceId);

        assertEquals(user, new User(data, userId, deviceId), "Failed with full set");
    }

    @Test
    public void UserIDTest() {
        UUID userId = UUID.randomUUID();
        UUID deviceId = UUID.randomUUID();
        User user = new User(NAME, EMAIL, PHONE, false, false, false, false, userId, deviceId);
        assertEquals(user.getUserId(), userId, "Failed with userId");
        assertEquals(user.getDeviceId(), deviceId, "Failed with deviceId");
    }

    @Test
    public void UserEqualityTest() {
        UUID userId = UUID.randomUUID();
        UUID deviceId = UUID.randomUUID();
        User userA = new User(NAME, EMAIL, PHONE, false, false, false, false, userId, deviceId);
        User userB = new User(NAME, EMAIL, PHONE, false, false, false, false, userId, deviceId);
        User userC = new User(NAME, EMAIL, PHONE, false, false, false, false);
        assertEquals(userA, userB);
        assertNotEquals(userA, userC);
        assertNotEquals(new Object(), userA);
    }
}
