package ca.quanta.quantaevents.models;

import java.util.UUID;

/**
 * Class which defines a comment.
 */
public class Comment {
    private UUID commentId;
    private UUID senderId;
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
    public Comment(UUID commentId, UUID senderId, String message, String postTime, String senderName) {
        this.commentId = commentId;
        this.senderId = senderId;
        this.message = message;
        this.postTime = postTime;
        this.senderName = senderName;
    }

    /**
     * Gets the ID of a comment.
     * @return UUID identifying the comment.
     */
    public UUID getCommentId() {
        return this.commentId;
    }

    /**
     * Sets the ID of a comment.
     * @param commentId New UUID to be assigned.
     */
    public void setCommentId(UUID commentId) {
        this.commentId = commentId;
    }

    /**
     * Gets the ID of the commenter.
     * @return UUID identifying commenter.
     */
    public UUID getSenderId() {
        return senderId;
    }

    /**
     * Sets the ID of a commenter.
     * @param senderId New UUID to be assigned.
     */
    public void setSenderId(UUID senderId) {
        this.senderId = senderId;
    }

    /**
     * Gets the comment's message.
     * @return String representing comment message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the comment's message.
     * @param message New message to be assigned to comment.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Gets the time the comment was posted.
     * @return String representing time comment was posted.
     */
    public String getPostTime() {
        return this.postTime;
    }

    /**
     * Sets the time the comment was posted.
     * @param postTime The new time to be set.
     */
    public void setPostTime(String postTime) {
        this.postTime = postTime;
    }

    /**
     * Gets the name of the commenter.
     * @return String representing name of the commenter.
     */
    public String getSenderName() {
        return this.senderName;
    }

    /**
     * Sets the name of the commenter.
     * @param senderName The new name to be set.
     */
    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }
}
