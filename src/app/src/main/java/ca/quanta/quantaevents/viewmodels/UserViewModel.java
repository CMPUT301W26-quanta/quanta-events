package ca.quanta.quantaevents.viewmodels;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import ca.quanta.quantaevents.models.ExternalUser;
import ca.quanta.quantaevents.models.User;

/**
 * View-model for managing user-related data and cloud functions.
 */
public class UserViewModel extends ViewModel {
    // Initialize an instance of cloud functions

    private FirebaseFunctions functions = FirebaseFunctions.getInstance();

    public UserViewModel() {
        functions.useEmulator("10.0.0.2", 5001);
    }

    /**
     * Calls the createUser cloud function, creating and adding a user to the database.
     * @param name             Name of the user.
     * @param email            Email address of the user.
     * @param phone            Phone number of the user.
     * @param isEntrant        Boolean that's true when the user selects to be an Entrant. (Will be removed in final checkpoint)
     * @param isOrganizer      Boolean that's true when the user selects to be an Organizer. (Will be removed in final checkpoint)
     * @param isAdmin          Boolean that's true when the user selects to be an Admin. (Will be removed in final checkpoint)
     * @param getNotifications Boolean that's true when the user selects to receive notifications. (Will be removed in final checkpoint)
     * @param deviceId         UUID identifying the user's device.
     * @return UUID assigned to newly created user.
     */
    public Task<UUID> createUser(String name, String email, String phone, Boolean isEntrant, Boolean isOrganizer, Boolean isAdmin, Boolean getNotifications, UUID deviceId) {
        // Arguments for createUser passed in data Map
        Map<String, Object> data = new HashMap<>();

        data.put("deviceId", deviceId.toString());
        data.put("name", name);
        data.put("email", email);
        data.put("phone", phone);
        data.put("receiveNotifications", getNotifications);
        data.put("isEntrant", isEntrant);
        data.put("isOrganizer", isOrganizer);
        data.put("isAdmin", isAdmin);

        return functions
                .getHttpsCallable("createUser")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, UUID>() {
                    // Access return value of the function createUser
                    @Override
                    public UUID then(@NonNull Task<HttpsCallableResult> task) {
                        Map<String, Object> result = (Map<String, Object>) task.getResult().getData();
                        UUID userId = UUID.fromString((String) result.get("userId"));
                        return userId;
                    }
                });
    }

    /**
     * Calls the getAllUsers cloud function, getting all users stored in the database.
     * @return List of User Objects.
     */
    public Task<ArrayList<ExternalUser>> getAllUsers(UUID userId, UUID deviceId) {
        Map<String, Object> data = new HashMap<>();

        data.put("userId", userId.toString());
        data.put("deviceId", deviceId.toString());

        Map<String, Object> payload = new HashMap<>();
        data.put("data", payload);

        return functions
               .getHttpsCallable("getAllUsers")
               .call(data)
               .continueWith(new Continuation<HttpsCallableResult, ArrayList<ExternalUser>>() {
                   @Override
                   public ArrayList<ExternalUser> then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                       List<Map<String, Object>> userObjects = (List<Map<String, Object>>) task.getResult().getData();
                       ArrayList<ExternalUser> users = new ArrayList<ExternalUser>();

                       for (Map<String, Object> userObject : userObjects) {
                           String userId = (String) userObject.get("userId");
                           String name = (String) userObject.get("name");
                           Boolean isEntrant = (Boolean) userObject.get("isEntrant");
                           Boolean isOrganizer = (Boolean) userObject.get("isOrganizer");
                           Boolean isAdmin = (Boolean) userObject.get("isAdmin");

                           // verify that they have a userId, otherwise just skip them

                           if (userId == null) {
                               continue;
                           }

                           UUID userUUID = UUID.fromString(userId);

                           users.add(new ExternalUser(userUUID, name, isEntrant, isOrganizer, isAdmin));
                       }

                       return users;
                   }
               });
    }

    /**
     * Calls the getUser cloud function, getting user data from the database.
     * @param userId   UUID to identify user.
     * @param deviceId UUID to identify user's device.
     * @return User object containing user's data.
     */
    public Task<User> getUser(UUID userId, UUID deviceId) {
        Map<String, Object> data = new HashMap<>();

        data.put("userId", userId.toString());
        data.put("deviceId", deviceId.toString());

        System.out.println("GET USER");

        return functions
                .getHttpsCallable("getUser")
                .call(data)
                .continueWith(task -> {
                    Map<String, Object> userData = (Map<String, Object>) task.getResult().getData();
                    User result = new User(userData, userId, deviceId);
                    System.out.println("TASK COMPLETE");
                    System.out.println("GOT " + result);
                    return result;
                });
    }

    /**
     * Calls the deleteUser cloud function, deleting a user from the database.
     * @param userId UUID identifying the user.
     * @param deviceId UUID identifying the user's device.
     * @return True if successful, and error if unsuccessful.
     */
    public Task<Boolean> deleteUser(UUID userId, UUID deviceId, UUID targetUserId) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId.toString());
        data.put("deviceId", deviceId.toString());

        Map<String, Object> payload = new HashMap<>();
        payload.put("target", targetUserId.toString());
        data.put("data", payload);

        return functions
                .getHttpsCallable("deleteUser")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, Boolean>() {
                    @Override
                    public Boolean then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        return true;
                    }
                });
    }

    /**
     * Calls the updateUser cloud function, updating existing user details.
     * @param userId UUID to identify the user.
     * @param deviceId UUID to verify users device.
     * @param name new user name (optional).
     * @param email new user email (optional).
     * @param phone Updated phone (optional).
     * @param receiveNotifications Updated notification preference (optional).
     * @return UUID identifying the user's ID.
     */
    public Task<Void> updateUser(UUID userId, UUID deviceId,
                                   @Nullable String name,
                                   @Nullable String email,
                                   @Nullable String phone,
                                   @Nullable Boolean receiveNotifications) {

        // Builds the data map to be sent to the function which contains
        // userId, deviceId and new details to update with.
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId.toString());
        data.put("deviceId", deviceId.toString());

        // payload... the details to update with.
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", name);
        payload.put("email", email);
        payload.put("phone", phone);
        payload.put("receiveNotifications", receiveNotifications);
        data.put("data", payload);

        // to debug code cuz a lot of errors while testing... T-T
        System.out.println("updateUser payload type=" + data.getClass().getName());
        System.out.println("updateUser payload=" + data);

        return functions
                .getHttpsCallable("updateUser")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, Void>() {
                    @Override
                    public Void then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        Map<String, Object> result = (Map<String, Object>) task.getResult().getData();
                        return null;
                    }
                });
    }

}
