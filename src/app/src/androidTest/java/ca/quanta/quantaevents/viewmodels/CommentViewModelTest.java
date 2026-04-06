package ca.quanta.quantaevents.viewmodels;

import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class CommentViewModelTest {
    private static final String EVENT_START = "2027-03-14T22:00:00.000Z";
    private static final String EVENT_END = "2027-04-20T22:00:00.000Z";
    private static final String EVENT_TIME = "2027-05-26T22:00:00.000Z";
    private static final String EVENT_NAME = "EventName";
    private static final String EVENT_DESCRIPTION = "Event Description";
    private static final Double EVENT_LOCATIONLAT = 53.5232723;
    private static final Double EVENT_LOCATIONLNG = -113.5262886;
    private static final String EVENT_CATEGORY = "Test Category";

    private static final String EVENT_GUIDELINES = "Test Guidlines";
    private static final Integer EVENT_REG_LIMIT = 100;

    private static final Integer EVENT_CAPACITY = 10;

    private static final UUIDHolder userHolder = new UUIDHolder();
    private static final UUIDHolder eventHolder = new UUIDHolder();

    private static final String USER_NAME = "INSTRUMENTED TEST USER";
    private static final String USER_EMAIL = "instrumented.user@gmail.com";
    private static final String USER_PHONE = "1234567890";
    private static final UUID USER_DEVICE_ID = new UUID(0, 0);

    private static final String COMMENT_MESSAGE_1 = "Test Comment 1";
    private static final String COMMENT_MESSAGE_2 = "Test Comment 2";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    CommentViewModel comments;

    public CommentViewModelTest() {
        comments = new CommentViewModel();
    }

    @BeforeClass
    public static void createTestDependencies() throws InterruptedException {
        TaskHandler.handle(new UserViewModel().createUser(USER_NAME, USER_EMAIL, USER_PHONE, false, USER_DEVICE_ID), userHolder::setUuid);

        TaskHandler.handle(
                new EventViewModel().createEvent(
                        userHolder.getUuid(),
                        USER_DEVICE_ID,
                        EVENT_START,
                        EVENT_END,
                        EVENT_TIME,
                        EVENT_NAME,
                        EVENT_DESCRIPTION,
                        EVENT_CATEGORY,
                        EVENT_GUIDELINES,
                        false,
                        EVENT_CAPACITY,
                        EVENT_LOCATIONLAT,
                        EVENT_LOCATIONLNG,
                        EVENT_REG_LIMIT,
                        null,
                        true),
                eventHolder::setUuid
        );
    }

    @AfterClass
    public static void deleteTestDependencies() throws InterruptedException {
        TaskHandler.handle(new UserViewModel().deleteUser(userHolder.getUuid(), USER_DEVICE_ID, userHolder.getUuid()), _void -> {
        });

        // Deleting account should delete event
    }

    @Test
    public void CreateAndGetCommentTest() throws InterruptedException {
        UUIDHolder holder1 = new UUIDHolder();
        UUIDHolder holder2 = new UUIDHolder();
        TaskHandler.handle(
                comments.createComment(
                        userHolder.getUuid(),
                        USER_DEVICE_ID,
                        eventHolder.getUuid(),
                        COMMENT_MESSAGE_1,
                        FORMATTER.format(LocalDateTime.now()
                        )
                ),
                holder1::setUuid
        );

        TaskHandler.handle(
                comments.createComment(
                        userHolder.getUuid(),
                        USER_DEVICE_ID,
                        eventHolder.getUuid(),
                        COMMENT_MESSAGE_2,
                        FORMATTER.format(LocalDateTime.now()
                        )
                ),
                holder2::setUuid
        );

        TaskHandler.handle(
                comments.getAllComments(
                        userHolder.getUuid(),
                        USER_DEVICE_ID,
                        eventHolder.getUuid()
                ),
                comments -> {
                    assertEquals(2, comments.size());
                    assertEquals(COMMENT_MESSAGE_1, comments.get(0).getMessage());
                    assertEquals(holder1.getUuid(), comments.get(0).getCommentId());
                    assertEquals(COMMENT_MESSAGE_2, comments.get(1).getMessage());
                    assertEquals(holder2.getUuid(), comments.get(1).getCommentId());
                }
        );

        TaskHandler.handle(comments.deleteComment(userHolder.getUuid(), USER_DEVICE_ID, eventHolder.getUuid(), holder1.getUuid()), _void -> {
        });
        TaskHandler.handle(comments.deleteComment(userHolder.getUuid(), USER_DEVICE_ID, eventHolder.getUuid(), holder2.getUuid()), _void -> {
        });
    }
}
