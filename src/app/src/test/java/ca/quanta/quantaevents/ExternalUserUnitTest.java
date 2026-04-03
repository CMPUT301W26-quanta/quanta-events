package ca.quanta.quantaevents;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import ca.quanta.quantaevents.models.ExternalUser;

public class ExternalUserUnitTest {
    private static final UUID USER_ID = UUID.randomUUID();
    private static final String NAME = "Test User";
    private static final String EMAIL = "test.user@email.tld";
    private static final String PHONE = "123-456-7890";
    private static final Boolean IS_ENTRANT = true;
    private static final Boolean IS_ORGANIZER = true;
    private static final Boolean IS_ADMIN = false;

    @Test
    public void ExternalUserCreationTest() {
        ExternalUser externalUser = new ExternalUser(USER_ID, NAME, EMAIL, PHONE, IS_ENTRANT, IS_ORGANIZER, IS_ADMIN);
        assertEquals(USER_ID, externalUser.getUserId());
        assertEquals(NAME, externalUser.getName());
        assertEquals(EMAIL, externalUser.getEmail());
        assertEquals(PHONE, externalUser.getPhone());
        assertEquals(IS_ENTRANT, externalUser.isEntrant());
        assertEquals(IS_ORGANIZER, externalUser.isOrganizer());
        assertEquals(IS_ADMIN, externalUser.isAdmin());
    }
}
