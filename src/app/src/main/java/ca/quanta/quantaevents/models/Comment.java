package ca.quanta.quantaevents.models;

import java.time.ZonedDateTime;
import java.util.UUID;

public class Comment{

    private final UUID commentId;
    private final UUID commenterId;
    private ZonedDateTime commentTime;


    /**
     * Constructor for a Notification object.
     * @param commenterId ID of a User who made the comment.
     * @param commentId The ID of the comment.
     * @param commentTime The time at which the comment was made.
     */
    public Comment(UUID commentId, UUID commenterId, ZonedDateTime commentTime){

        this.commentId = UUID.randomUUID();
        this.commenterId = commenterId;
        this.commentTime = commentTime;
    }

    public UUID getCommenterId() {
        return this.commenterId;
    }

    public UUID getCommentId(){
        return this.commentId;
    }

    public ZonedDateTime getCommentTime(){
        return this.commentTime;
    }


}
