package ca.quanta.quantaevents.models;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.UUID;


/**
 * Class which defines a User of the app.
 */
public class User {

    /**
     * Class which defines an Entrant.
     */
    public static class Entrant {

        private ArrayList<UUID> enteredEvents;
        private Boolean receiveNotifications;

        /**
         * Constructor for an Entrant object.
         *
         * @param receiveNotifications Boolean that's true when the entrant wants to receive notifications, false otherwise.
         */
        Entrant(Boolean receiveNotifications) {
            this.enteredEvents = new ArrayList<>();
            this.receiveNotifications = receiveNotifications;
        }

        public boolean getReceiveNotifications() {
            return receiveNotifications;
        }
    }

    /**
     * Class which defines an Organizer.
     */
    public static class Organizer {

        private ArrayList<UUID> createdEvents;
        private ArrayList<UUID> sentNotifications;

        /**
         * Constructor for an Organizer object.
         */
        Organizer() {
            this.createdEvents = new ArrayList<>();
            this.sentNotifications = new ArrayList<>();
        }

    }

    /**
     * Class which defines an Admin.
     */
    public static class Admin {
    }

    @Nullable
    Entrant entrant;
    @Nullable
    Organizer organizer;
    @Nullable
    Admin admin;

    private final UUID userId;
    private final UUID deviceId;
    private String name;
    private String email;
    @Nullable
    private String phoneNumber;

    /**
     * Constructor for a User object.
     *
     * @param name                 Name of the user.
     * @param email                Email address of the user.
     * @param phoneNumber          Phone number of the user.
     * @param receiveNotifications Boolean that's true when the user wants to receive notifications, false otherwise.
     * @param isEntrant            Boolean that's true if the user is an Entrant, false otherwise.
     * @param isOrganizer          Boolean that's true if the user is an Organizer, false otherwise.
     * @param isAdmin              Boolean that's true if the user is an Admin, false otherwise.
     */
    public User(String name, String email, @Nullable String phoneNumber, Boolean receiveNotifications,
                Boolean isEntrant, Boolean isOrganizer, Boolean isAdmin) {
        this.userId = UUID.randomUUID();
        this.deviceId = UUID.randomUUID();
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;

        /*
        The isEntrant, isOrganizer, and isAdmin parameters may be removed later
        once we figure out how we want to determine who gets what role when someone
        makes their account
        */

        if (isEntrant) {
            this.entrant = new Entrant(receiveNotifications);
        } else {
            this.entrant = null;
        }

        if (isOrganizer) {
            this.organizer = new Organizer();
        } else {
            this.organizer = null;
        }

        if (isAdmin) {
            this.admin = new Admin();
        } else {
            this.admin = null;
        }

    }

    /**
     * Constructor for a User object when user and device ID are known.
     *
     * @param name                 Name of the user.
     * @param email                Email address of the user.
     * @param phoneNumber          Phone number of the user.
     * @param receiveNotifications Boolean that's true when the user wants to receive notifications, false otherwise.
     * @param isEntrant            Boolean that's true if the user is an Entrant, false otherwise.
     * @param isOrganizer          Boolean that's true if the user is an Organizer, false otherwise.
     * @param isAdmin              Boolean that's true if the user is an Admin, false otherwise.
     * @param userId               The user's ID
     * @param deviceId             The user's device ID
     */
    public User(String name, String email, @Nullable String phoneNumber, Boolean receiveNotifications,
                Boolean isEntrant, Boolean isOrganizer, Boolean isAdmin, UUID userId, UUID deviceId) {
        this.userId = userId;
        this.deviceId = deviceId;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;

        /*
        The isEntrant, isOrganizer, and isAdmin parameters may be removed later
        once we figure out how we want to determine who gets what role when someone
        makes their account
        */

        if (isEntrant) {
            this.entrant = new Entrant(receiveNotifications);
        } else {
            this.entrant = null;
        }

        if (isOrganizer) {
            this.organizer = new Organizer();
        } else {
            this.organizer = null;
        }

        if (isAdmin) {
            this.admin = new Admin();
        } else {
            this.admin = null;
        }

    }

    public boolean isEntrant() {
        return entrant != null;
    }

    @Nullable
    public Entrant getEntrant() {
        return entrant;
    }

    public boolean isOrganizer() {
        return organizer != null;
    }

    public boolean isAdmin() {
        return admin != null;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getDeviceId() {
        return deviceId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    @Nullable
    public String getPhoneNumber() {
        return phoneNumber;
    }
}