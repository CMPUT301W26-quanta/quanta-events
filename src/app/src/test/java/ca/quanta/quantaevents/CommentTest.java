package ca.quanta.quantaevents;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import ca.quanta.quantaevents.models.Comment;

public class CommentTest {
    private static final UUID COMMENT_ID = UUID.randomUUID();
    private static final UUID SENDER_ID = UUID.randomUUID();
    private static final String MESSAGE = "Test message";
    private static final String POST_TIME = "Test message";
    private static final String SENDER_NAME = "Test message";

    @Test
    public void CommentCreationTest() {
        Comment comment = new Comment(COMMENT_ID, SENDER_ID, MESSAGE, POST_TIME, SENDER_NAME);
        assertEquals(COMMENT_ID, comment.getCommentId());
        assertEquals(SENDER_ID, comment.getSenderId());
        assertEquals(MESSAGE, comment.getMessage());
        assertEquals(POST_TIME, comment.getPostTime());
        assertEquals(SENDER_NAME, comment.getSenderName());
    }
}
