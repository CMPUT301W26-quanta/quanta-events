package ca.quanta.quantaevents.models;

import java.util.UUID;

public class Comment {
    private String commentId;
    private String senderId;
    private String message;

    String postTime;

    String senderName;

    public Comment() {}

    /**
     * Constructor for a Comment object.
     * @param commentId The ID of the comment.
     * @param senderId ID of a user who made the comment.
     * @param message The content of the comment.
     * @param postTime The time at which the comment was made.
     * @param senderName The name of the user who made the comment.
     */
    public Comment(String commentId, String senderId, String message, String postTime, String senderName) {
        this.commentId = commentId;
        this.senderId = senderId;
        this.message = message;
        this.postTime = postTime;
        this.senderName = senderName;
    }

    public String getCommentId() {
        return this.commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPostTime() {
        return this.postTime;
    }

    public void setPostTime(String postTime) {
        this.postTime = postTime;
    }

    public String getSenderName() {
        return this.senderName;
    }
    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }
}
