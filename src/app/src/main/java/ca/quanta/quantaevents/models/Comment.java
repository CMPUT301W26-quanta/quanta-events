package ca.quanta.quantaevents.models;

import java.time.ZonedDateTime;
import java.util.UUID;

public class Comment{

    private final UUID commentId;
    private final UUID userId;
    private ZonedDateTime commentTime;

    public Comment(UUID commentId, UUID userId, ZonedDateTime commentTime){

        this.commentId = UUID.randomUUID();
        this.userId = userId;
        this.commentTime = commentTime;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getCommentId(){
        return commentId;
    }

    public ZonedDateTime getCommentTime(){
        return commentTime;
    }


}
