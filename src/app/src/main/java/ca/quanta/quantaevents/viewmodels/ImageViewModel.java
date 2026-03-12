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
 * Class which represents an ImageViewModel object.
 */
public class ImageViewModel extends ViewModel {

    // Initialize an instance of cloud functions
    private FirebaseFunctions functions = FirebaseFunctions.getInstance();

    /**
     * Calls the createImage cloud function, adding an image to the database.
     * @param userId UUID identifying the user.
     * @param deviceId UUID identifying the user's device.
     * @param imageData Base64-encoded image data.
     * @return UUID identifying the image's ID.
     */
    public Task<String> createImage(UUID userId, UUID deviceId, String imageData) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId.toString());
        data.put("deviceId", deviceId.toString());

        Map<String, Object> payload = new HashMap<>();
        payload.put("imageData", imageData);
        data.put("data", payload);

        return functions
                .getHttpsCallable("createImage")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        Map<String, Object> result = (Map<String, Object>) task.getResult().getData();
                        return (String) result.get("imageId");
                    }
                });
    }

    /**
     * Calls the getImage cloud function, fetching an image from the database.
     * @param imageId UUID identifying the image.
     * @param userId UUID identifying the user.
     * @param deviceId UUID identifying the user's device.
     * @return Map containing the image's data.
     */
    public Task<Map<String, Object>> getImage(UUID imageId, UUID userId, UUID deviceId) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId.toString());
        data.put("deviceId", deviceId.toString());

        Map<String, Object> payload = new HashMap<>();
        payload.put("imageId", imageId.toString());
        data.put("data", payload);

        return functions
                .getHttpsCallable("getImage")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, Map<String, Object>>() {
                    @Override
                    public Map<String, Object> then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        return (Map<String, Object>) task.getResult().getData();
                    }
                });
    }
}
