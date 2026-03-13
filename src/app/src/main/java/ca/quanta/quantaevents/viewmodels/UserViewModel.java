package ca.quanta.quantaevents.viewmodels;

import android.util.Log;

import androidx.annotation.NonNull;
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

import ca.quanta.quantaevents.models.User;

/**
 * Class which represents a UserViewModel object.
 */
public class UserViewModel extends ViewModel {
    // Initialize an instance of cloud functions

    /**
     * Calls the createUser cloud function, adding a user to the database.
     *
     * @param name             Name of the user.
     * @param email            Email address of the user.
     * @param phone            Phone number of the user.
     * @param isEntrant        Boolean that's true when the user selects to be an Entrant.
     * @param isOrganizer      Boolean that's true when the user selects to be an Organizer.
     * @param isAdmin          Boolean that's true when the user selects to be an Admin.
     * @param getNotifications Boolean that's true when the user selects to receive notifications.
     * @param deviceId         UUID identifying the user's device.
     * @return UUID identifying the user's ID.
     */
    public Task<UUID> createUser(String name, String email, String phone, Boolean isEntrant, Boolean isOrganizer, Boolean isAdmin, Boolean getNotifications, UUID deviceId) {
        // Arguments for createUser
        Map<String, Object> data = new HashMap<>();

        data.put("deviceId", deviceId.toString());
        data.put("name", name);
        data.put("email", email);
        data.put("phone", phone);
        data.put("receiveNotifications", getNotifications);
        data.put("isEntrant", isEntrant);
        data.put("isOrganizer", isOrganizer);
        data.put("isAdmin", isAdmin);

        return FirebaseFunctions.getInstance()
                .getHttpsCallable("createUser")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, UUID>() {
                    // Access return value of the function
                    @Override
                    public UUID then(@NonNull Task<HttpsCallableResult> task) {
                        Map<String, Object> result = (Map<String, Object>) task.getResult().getData();
                        UUID userId = UUID.fromString((String) result.get("userId"));
                        return userId;
                    }
                });
    }

    public Task<ArrayList<User>> getAllUsers() {
        FirebaseFunctions functions = FirebaseFunctions.getInstance();

       return functions
               .getHttpsCallable("getAllUsers")
               .call(new HashMap<>())
               .continueWith(new Continuation<HttpsCallableResult, ArrayList<User>>() {
                   @Override
                   public ArrayList<User> then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                       List<Map<String, Object>> userObjects = (List<Map<String, Object>>) task.getResult().getData();
                       ArrayList<User> users = new ArrayList<User>();

                       for (Map<String, Object> userObject : userObjects) {
                           // we only get a limited amount of info back about each user

                           String userId = (String) userObject.get("userId");
                           String deviceId = (String) userObject.get("deviceId");
                           String name = (String) userObject.get("name");
                           Boolean isEntrant = (Boolean) userObject.get("isEntrant");
                           Boolean isOrganizer = (Boolean) userObject.get("isOrganizer");
                           Boolean isAdmin = (Boolean) userObject.get("isAdmin");

                           // verify that they have a userId and deviceId
                           // otherwise just skip them

                           if (userId == null) {
                               continue;
                           }

                           if (deviceId == null) {
                               continue;
                           }

                           UUID userUUID = UUID.fromString(userId);
                           UUID deviceUUID = UUID.fromString(deviceId);

                           users.add(new User(name, null, null, null, isEntrant, isOrganizer, isAdmin, userUUID, deviceUUID));
                       }

                       return users;
                   }
               });
    }

    /**
     * Calls the getUser cloud function, getting a user's details from the database.
     *
     * @param userId   UUID identifying the user.
     * @param deviceId UUID identifying the user's device.
     * @return User object with the user's information.
     */
    public Task<User> getUser(UUID userId, UUID deviceId) {
        Map<String, Object> data = new HashMap<>();

        data.put("userId", userId.toString());
        data.put("deviceId", deviceId.toString());

        System.out.println("GET USER");

        return FirebaseFunctions.getInstance()
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
     * Calls the deleteUser cloud function, deleting a user.
     * @param userId UUID identifying the user.
     * @param deviceId UUID identifying the user's device.
     * @return True when delete completes.
     */
    public Task<Boolean> deleteUser(UUID userId, UUID deviceId, UUID targetUserId) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId.toString());
        data.put("deviceId", deviceId.toString());

        Map<String, Object> payload = new HashMap<>();
        payload.put("target", targetUserId.toString());
        data.put("data", payload);

        return FirebaseFunctions.getInstance()
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
     * Calls the updateUser cloud function, updating a user's details.
     * @param userId UUID identifying the user.
     * @param deviceId UUID identifying the user's device.
     * @param name Updated user name.
     * @param email Updated user email.
     * @param phone Updated phone (optional).
     * @param receiveNotifications Updated notification preference (optional).
     * @return UUID identifying the user's ID.
     */
    public Task<String> updateUser(UUID userId, UUID deviceId, String name, String email,
                                   String phone, Boolean receiveNotifications) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId.toString());
        data.put("deviceId", deviceId.toString());

        Map<String, Object> payload = new HashMap<>();
        payload.put("name", name);
        payload.put("email", email);
        payload.put("phone", phone);
        payload.put("receiveNotifications", receiveNotifications);
        data.put("data", payload);

        return FirebaseFunctions.getInstance()
                .getHttpsCallable("updateUser")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        Map<String, Object> result = (Map<String, Object>) task.getResult().getData();
                        return (String) result.get("userId");
                    }
                });
    }

    /**
     * Calls the getUser cloud function, returning the raw user map.
     *
     * @param userId   UUID identifying the user.
     * @param deviceId UUID identifying the user's device.
     * @return Map containing the user's data.
     */
    public Task<Map<String, Object>> getUserRaw(UUID userId, UUID deviceId) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId.toString());
        data.put("deviceId", deviceId.toString());

        return FirebaseFunctions.getInstance()
                .getHttpsCallable("getUser")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, Map<String, Object>>() {
                    @Override
                    public Map<String, Object> then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        return (Map<String, Object>) task.getResult().getData();
                    }
                });
    }

}
