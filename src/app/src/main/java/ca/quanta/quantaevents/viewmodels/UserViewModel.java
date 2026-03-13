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

import ca.quanta.quantaevents.models.User;

/**
 * Class which represents a UserViewModel object for the project and
 * import all user related functions.
 */
public class UserViewModel extends ViewModel {
    // Initialize an instance of cloud functions

    private FirebaseFunctions functions = FirebaseFunctions.getInstance();

    /**
     * Calls the createUser cloud function and adds a user to the database
     *
     * @param name             Name of the user.
     * @param email            Email address of the user.
     * @param phone            Phone number of the user.
     * @param isEntrant        Boolean that's true when the user selects to be an Entrant. (Will be removed in fina checkpoint)
     * @param isOrganizer      Boolean that's true when the user selects to be an Organizer. (Will be removed in fina checkpoint)
     * @param isAdmin          Boolean that's true when the user selects to be an Admin. (Will be removed in fina checkpoint)
     * @param getNotifications Boolean that's true when the user selects to receive notifications. (Will be removed in fina checkpoint)
     * @param deviceId         UUID identifying to identify the users device.
     * @return UUID which is used to locate the user on firestore.
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
     * Calls the getAllUsers cloud function and returns a list of users in the database.
     *
     * @return List of User Objects which have all information related to the user.
     */
    public Task<ArrayList<User>> getAllUsers() {
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

                           UUID userUUID = UUID.fromString(userId);
                           UUID deviceUUID = UUID.fromString(deviceId);

                           users.add(new User(name, null, null, null, isEntrant, isOrganizer, isAdmin, userUUID, deviceUUID));
                       }

                       return users;
                   }
               });
    }

    /**
     * Calls the getUser cloud function gets information about the user from the database
     *
     * @param userId   UUID to identify the record stored in Users collection.
     * @param deviceId UUID to identify if the deviceId matches what we have on database for the user.
     * @return User object with the user's information.
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
     * Calls the deleteUser cloud function, deleting a user.
     * @param userId UUID identifying the user.
     * @param deviceId UUID identifying the user's device.
     * @return True when delete completes successfully without errors else returns the error.
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
     * Calls the updateUser cloud function updates existing users details.
     * @param userId UUID to identify the user in database.
     * @param deviceId UUID to verify users device.
     * @param name new user name (optional)/
     * @param email new user email (optional).
     * @param phone Updated phone (optional).
     * @param receiveNotifications Updated notification preference (optional).
     * @return UUID identifying the user's ID.
     */
    public Task<String> updateUser(UUID userId, UUID deviceId,
                                   @Nullable String name,
                                   @Nullable String email,
                                   @Nullable String phone,
                                   @Nullable Boolean receiveNotifications) {

        // Builds the data map to be sent to the functionnn which contains
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
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        Map<String, Object> result = (Map<String, Object>) task.getResult().getData();
                        return (String) result.get("userId");
                    }
                });
    }

    /**
     * Calls the getUser cloud function returns the raw user map
     *
     * @param userId UUID to identify the user in database.
     * @param deviceId UUID to verify users device.
     * @return A Map containing the users data returned from the function.
     */
    public Task<Map<String, Object>> getUserRaw(UUID userId, UUID deviceId) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId.toString());
        data.put("deviceId", deviceId.toString());

        return functions
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
