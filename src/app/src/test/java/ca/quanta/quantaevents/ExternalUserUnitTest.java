package ca.quanta.quantaevents;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import ca.quanta.quantaevents.models.ExternalUser;

public class ExternalUserUnitTest {
    private static final UUID USER_ID = UUID.randomUUID();
    private static final String NAME = "Test User";

    @Test
    public void ExternalUserCreationTest() {
        ExternalUser externalUser = new ExternalUser(USER_ID, NAME);
        assertEquals(USER_ID, externalUser.getUserId());
        assertEquals(NAME, externalUser.getName());
    }
}
