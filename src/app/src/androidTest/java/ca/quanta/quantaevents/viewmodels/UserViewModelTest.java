package ca.quanta.quantaevents.viewmodels;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.UUID;

public class UserViewModelTest {
    private static final String NAME = "INSTRUMENTED TEST USER";
    private static final String EMAIL = "instrumented.user@gmail.com";
    private static final String PHONE = "1234567890";
    private static final UUID DEVICE_ID = new UUID(0, 0);

    UserViewModel users;
    private static final UUIDHolder holder = new UUIDHolder(null);

    public UserViewModelTest() {
        users = new UserViewModel();
    }

    @BeforeClass
    public static void createTestUser() throws InterruptedException {
        TaskHandler.handle(new UserViewModel().createUser(NAME, EMAIL, PHONE, false, DEVICE_ID), holder::setUuid);
    }

    @AfterClass
    public static void deleteTestUser() throws InterruptedException {
        TaskHandler.handle(new UserViewModel().deleteUser(holder.getUuid(), DEVICE_ID, holder.getUuid()), _void -> {
        });
    }

    @Test
    public void CreateDeleteUserTest() throws InterruptedException {
        UUIDHolder holder = new UUIDHolder(null);

        TaskHandler.handle(users.createUser(NAME, EMAIL, PHONE, false, DEVICE_ID), holder::setUuid);

        TaskHandler.handle(users.deleteUser(holder.getUuid(), DEVICE_ID, holder.getUuid()), _void -> {
        });
    }

    @Test
    public void GetUserTest() throws InterruptedException {
        TaskHandler.handle(users.getUser(holder.getUuid(), DEVICE_ID), user -> {
            assertEquals("Failed with name", NAME, user.getName());
            assertEquals("Failed with email", EMAIL, user.getEmail());
            assertEquals("Failed with phone", PHONE, user.getPhoneNumber());
            assertTrue(user.isEntrant());
            assertTrue(user.isOrganizer());
            assertFalse(user.isAdmin());
            assertNotNull(user.getEntrant());
            assertFalse(user.getEntrant().getReceiveNotifications());
        });
    }

    @Test
    public void UpdateUserTest() throws InterruptedException {
        TaskHandler.handle(users.updateUser(holder.getUuid(), DEVICE_ID, NAME + " UPDATE", EMAIL, PHONE, false), _void -> {
        });

        TaskHandler.handle(users.getUser(holder.getUuid(), DEVICE_ID), user -> {
            assertEquals("Failed with name", NAME + " UPDATE", user.getName());
            assertEquals("Failed with email", EMAIL, user.getEmail());
            assertEquals("Failed with phone", PHONE, user.getPhoneNumber());
            assertTrue(user.isEntrant());
            assertTrue(user.isOrganizer());
            assertFalse(user.isAdmin());
            assertNotNull(user.getEntrant());
            assertFalse(user.getEntrant().getReceiveNotifications());
        });
    }
}
