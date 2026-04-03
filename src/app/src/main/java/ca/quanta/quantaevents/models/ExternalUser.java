package ca.quanta.quantaevents.models;

import java.util.UUID;

/**
 * Class which defines a reference to another user with some basic info.
 */
public class ExternalUser {

    private UUID userId;
    private String name;
    private Boolean isEntrant;
    private Boolean isOrganizer;
    private Boolean isAdmin;
    private String email;
    private String phone;

    /**
     * Constructor for an ExternalUser object.
     *
     * @param userId Unique ID of the user.
     * @param name   Name of the user.
     * @param isEntrant Whether or not the user is an entrant.
     * @param isOrganizer Whether or not the user is an organizer.
     * @param isAdmin Whether or not hte user is an administrator.
     */
    public ExternalUser(UUID userId, String name, Boolean isEntrant, Boolean isOrganizer, Boolean isAdmin) {
        this.userId = userId;
        this.name = name;
        this.isEntrant = isEntrant;
        this.isOrganizer = isOrganizer;
        this.isAdmin = isAdmin;
    }

    public UUID getUserId() {
        return this.userId;
    }

    public String getName() {
        return this.name;
    }

    public Boolean isEntrant() {
        return this.isEntrant;
    }

    public Boolean isOrganizer() {
        return this.isOrganizer;
    }

    public Boolean isAdmin() {
        return this.isAdmin;
    }

    public String getEmail() {
        return this.email;
    }

    public String getPhone() {
        return this.phone;
    }


    /**
     * Constructor for an ExternalUser but has name, phone and email
     *
     * @param userId Unique ID of the user.
     * @param name   Name of the user.
     * @param email  Email of the user.
     * @param phone  Phone number of the user.
     */
    public ExternalUser(UUID userId, String name, String email, String phone) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.isEntrant = null;
        this.isOrganizer = null;
        this.isAdmin = null;
    }
}