package ca.quanta.quantaevents.models;

import java.util.UUID;

public class Comment {
    private UUID commentId;
    private UUID senderId;
    private String message;

    String postTime;

    String senderName;

    /**
     * Constructor for a Comment object.
     *
     * @param commentId  The ID of the comment.
     * @param senderId   ID of a user who made the comment.
     * @param message    The content of the comment.
     * @param postTime   The time at which the comment was made.
     * @param senderName The name of the user who made the comment.
     */
    public Comment(UUID commentId, UUID senderId, String message, String postTime, String senderName) {
        this.commentId = commentId;
        this.senderId = senderId;
        this.message = message;
        this.postTime = postTime;
        this.senderName = senderName;
    }

    public UUID getCommentId() {
        return this.commentId;
    }

    public UUID getSenderId() {
        return senderId;
    }

    public String getMessage() {
        return message;
    }

    public String getPostTime() {
        return this.postTime;
    }

    public String getSenderName() {
        return this.senderName;
    }
}
