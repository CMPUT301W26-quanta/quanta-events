package ca.quanta.quantaevents.models;

import java.time.ZonedDateTime;
import java.util.UUID;

public class Comment {

    private final UUID commentId;
    private final UUID senderId;
    private final String message;


    /**
     * Constructor for a Notification object.
     * @param commentId The ID of the comment.
     * @param senderId ID of a User who made the comment.
     * @param message The content of the comment.
     */
    public Comment(UUID commentId, UUID senderId, String message){

        this.commentId = commentId;
        this.senderId = senderId;
        this.message = message;
    }

    public UUID getCommentId(){
        return this.commentId;
    }

    public UUID getSenderId() {
        return this.senderId;
    }

    public String getMessage(){
        return this.message;
    }
}
