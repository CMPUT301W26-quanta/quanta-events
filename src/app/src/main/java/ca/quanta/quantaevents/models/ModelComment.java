package ca.quanta.quantaevents.models;

import android.graphics.ColorSpace;

import java.util.UUID;

public class ModelComment {

    String commentId;

    String comment;

    String postTime;

    String userId;

    String userName;

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId){
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


    public String getUserId(){
        return userId;
    }

    public void setUserId(String userId){
        this.userId = userId;
    }

    public String getUserName(){
        return userName;
    }
    public void setUserName(String userName){
        this.userName = userName;
    }

    public ModelComment(){
    }

    public ModelComment(String commentId, String comment, String postTime, String userName, String userId) {
        this.commentId = commentId;
        this.comment = comment;
        this.postTime = postTime;
        this.userName = userName;
        this.userId = userId;


    }
}
