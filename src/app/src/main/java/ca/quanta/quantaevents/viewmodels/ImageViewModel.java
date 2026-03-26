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

import ca.quanta.quantaevents.models.Image;
import ca.quanta.quantaevents.models.User;

/**
 * View-model for managing image-related data and cloud functions.
 */
public class ImageViewModel extends ViewModel {
    // Initialize an instance of cloud functions
    private FirebaseFunctions functions = FirebaseFunctions.getInstance();

    public ImageViewModel() {
        functions.useEmulator("10.0.0.2", 5001);
    }

    /**
     * Calls the createImage cloud function, creating and adding an image to the database.
     * @param userId UUID to identify user.
     * @param deviceId UUID to identify the user's device.
     * @param imageData Base64-encoded image data.
     * @return UUID assigned to newly created image.
     */
    public Task<UUID> createImage(UUID userId, UUID deviceId, String imageData) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId.toString());
        data.put("deviceId", deviceId.toString());

        Map<String, Object> payload = new HashMap<>();
        payload.put("imageData", imageData);
        data.put("data", payload);

        return functions
                .getHttpsCallable("createImage")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, UUID>() {
                    @Override
                    public UUID then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        Map<String, Object> result = (Map<String, Object>) task.getResult().getData();
                        return UUID.fromString((String) result.get("imageId"));
                    }
                });
    }

    /**
     * Calls the getImage cloud function and fetches the image from the database.
     * @param imageId UUID to identify the image
     * @param userId UUID to identify user.
     * @param deviceId UUID to identify the user's device.
     * @return Map containing image data.
     */
    public Task<Image> getImage(UUID imageId, UUID userId, UUID deviceId) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId.toString());
        data.put("deviceId", deviceId.toString());

        Map<String, Object> payload = new HashMap<>();
        payload.put("imageId", imageId.toString());
        data.put("data", payload);

        return functions
                .getHttpsCallable("getImage")
                .call(data)
                .continueWith(task -> {
                    Map<String, Object> imageData = (Map<String, Object>) task.getResult().getData();
                    Image result = new Image(imageId, imageData);
                    System.out.println("Got Image" + result);
                    return result;
                });

    }

    /**
     * Calls the deleteImage cloud function and deletes the image (and all references to it)
     * from the database.
     * @param imageId UUID to identify the image to delete.
     * @param userId UUID to identify this user.
     * @param deviceId UUID to identify this user's device.
     * @return True if successful, and error if unsuccessful.
     */
    public Task<Boolean> deleteImage(UUID imageId, UUID userId, UUID deviceId) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId.toString());
        data.put("deviceId", deviceId.toString());

        Map<String, Object> payload = new HashMap<>();
        payload.put("imageId", imageId.toString());
        data.put("data", payload);

        return functions
                .getHttpsCallable("deleteImage")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, Boolean>() {
                    @Override
                    public Boolean then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        return true;
                    }
                });
    }
}
