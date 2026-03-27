package ca.quanta.quantaevents.viewmodels;

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

import ca.quanta.quantaevents.models.Comment;

/**
 * View-model for managing comment-related data and cloud functions.
 */
public class CommentViewModel extends ViewModel {
    // Initialize an instance of cloud functions
    private FirebaseFunctions functions = FirebaseFunctions.getInstance();

    public CommentViewModel() {
        functions.useEmulator("10.0.2.2", 5001);
    }

    /**
     * Calls the createComment cloud function, creating and adding a comment to the database.
     * @param userId This user's id (for permissions checking).
     * @param deviceId This user's device id (for permissions checking).
     * @param eventId The event to post the comment to.
     * @param message The content of the comment.
     * @return The ID of the created comment.
     */
    public Task<UUID> createComment(UUID userId, UUID deviceId, UUID eventId, String message) {
        Map<String, Object> data = new HashMap<>();

        data.put("userId", userId.toString());
        data.put("deviceId", deviceId.toString());

        Map<String, Object> payload = new HashMap<>();
        payload.put("eventId", eventId.toString());
        payload.put("message", message);

        data.put("data", payload);

        return functions
                .getHttpsCallable("createComment")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, UUID>() {
                    // Access return value of the function createUser
                    @Override
                    public UUID then(@NonNull Task<HttpsCallableResult> task) {
                        String result = (String) task.getResult().getData();
                        return UUID.fromString(result);
                    }
                });
    }

    /**
     * Calls the getAllComments cloud function, getting all comments stored in the database.
     * @param userId This user's id (for permissions checking).
     * @param deviceId This user's device id (for permissions checking).
     * @param eventId The ID of the event under which to retrieve all the comments for.
     * @return List of Comment Objects.
     */
    public Task<ArrayList<Comment>> getAllComments(UUID userId, UUID deviceId, UUID eventId) {
        Map<String, Object> data = new HashMap<>();

        data.put("userId", userId.toString());
        data.put("deviceId", deviceId.toString());

        Map<String, Object> payload = new HashMap<>();
        payload.put("eventId", eventId.toString());

        data.put("data", payload);

        return functions
                .getHttpsCallable("getAllComments")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, ArrayList<Comment>>() {
                    @Override
                    public ArrayList<Comment> then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        List<Map<String, Object>> commentObjects = (List<Map<String, Object>>) task.getResult().getData();
                        ArrayList<Comment> comments = new ArrayList<>();

                        for (Map<String, Object> commentObject : commentObjects) {
                            UUID commentId = UUID.fromString((String) commentObject.get("commentId"));
                            UUID senderId = UUID.fromString((String) commentObject.get("senderId"));
                            String message = (String) commentObject.get("message");

                            comments.add(new Comment(commentId, senderId, message));
                        }

                        return comments;
                    }
                });
    }

    /**
     * Calls the deleteComment cloud function, deleting a comment from the database.
     * @param userId This user's id (for permissions checking).
     * @param deviceId This user's device id (for permissions checking).
     * @param eventId The ID of the event under which to delete the comments.
     * @param commentId The ID of the comment to delete.
     * @return Nothing.
     */
    public Task<Void> deleteComment(UUID userId, UUID deviceId, UUID eventId, UUID commentId) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId.toString());
        data.put("deviceId", deviceId.toString());

        Map<String, Object> payload = new HashMap<>();
        payload.put("eventId", eventId.toString());
        payload.put("commentId", commentId.toString());

        data.put("data", payload);

        return functions
                .getHttpsCallable("deleteComment")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, Void>() {
                    @Override
                    public Void then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        return null;
                    }
                });
    }
}
