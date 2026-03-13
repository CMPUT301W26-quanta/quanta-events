package ca.quanta.quantaevents.models;

import java.util.UUID;

/**
 * Class which defines a reference to another user with some basic info.
 */
public class ExternalUser {

    private UUID userId;
    private String name;

    /**
     * Constructor for an ExternalUser object.
     *
     * @param userId Unique ID of the user.
     * @param name   Name of the user.
     */
    public ExternalUser(UUID userId, String name) {
        this.userId = userId;
        this.name = name;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }
}