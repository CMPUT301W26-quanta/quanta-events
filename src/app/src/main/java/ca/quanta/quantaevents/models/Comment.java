package ca.quanta.quantaevents.models;

import java.util.UUID;

public class Comment {
    UUID commentId;

    String comment;

    String postTime;

    UUID userId;

    String userName;

    UUID deviceId;

    public UUID getCommentId() {
        return commentId;
    }

    public void setCommentId(UUID commentId){
        this.commentId = commentId;
    }

    public String getComment(){
        return comment;
    }

    public void setComment(String comment){
        this.comment = comment;
    }

    public String getPostTime(){
        return postTime;
    }

    public void setPostTime(String postTime){
        this.postTime = postTime;
    }


    public UUID getUserId(){
        return userId;
    }

    public void setUserId(UUID userId){
        this.userId = userId;
    }

    public String getUserName(){
        return userName;
    }
    public void setUserName(String userName){
        this.userName = userName;
    }

    public UUID getDeviceId(){
        return deviceId;
    }
    public void setDeviceId(UUID deviceId){
        this.deviceId = deviceId;
    }

    public Comment(){
    }

    public Comment(UUID commentId, String comment, String postTime, String userName, UUID userId, UUID deviceId) {
        this.commentId = commentId;
        this.comment = comment;
        this.postTime = postTime;
        this.userName = userName;
        this.userId = userId;
        this.deviceId = deviceId;


    }
}
