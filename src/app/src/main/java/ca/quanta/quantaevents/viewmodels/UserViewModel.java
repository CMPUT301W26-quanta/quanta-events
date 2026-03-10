package ca.quanta.quantaevents.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Class which represents a UserViewModel object.
 */
public class UserViewModel extends ViewModel {

    // Initialize an instance of cloud functions
    private FirebaseFunctions functions = FirebaseFunctions.getInstance();

    /**
     * Calls the createUser cloud function, adding a user to the database.
     * @param name Name of the user.
     * @param email Email address of the user.
     * @param phone Phone number of the user.
     * @param isEntrant Boolean that's true when the user selects to be an Entrant.
     * @param isOrganizer Boolean that's true when the user selects to be an Organizer.
     * @param isAdmin Boolean that's true when the user selects to be an Admin.
     * @param getNotifications Boolean that's true when the user selects to receive notifications.
     * @param deviceId UUID identifying the user's device.
     * @return UUID identifying the user's ID.
     */
    public Task<String> createUser(String name, String email, String phone, Boolean isEntrant, Boolean isOrganizer, Boolean isAdmin, Boolean getNotifications, UUID deviceId) {
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

        return functions
                .getHttpsCallable("createUser")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    // Access return value of the function
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        Map<String, Object> result = (Map<String, Object>) task.getResult().getData();
                        return (String) result.get("userId");
                    }
                });
    }

}