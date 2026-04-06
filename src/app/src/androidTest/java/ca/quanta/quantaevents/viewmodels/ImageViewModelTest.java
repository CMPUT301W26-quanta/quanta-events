package ca.quanta.quantaevents.viewmodels;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.UUID;

public class ImageViewModelTest {
    private static final UUIDHolder userHolder = new UUIDHolder();

    private static final String USER_NAME = "INSTRUMENTED TEST USER";
    private static final String USER_EMAIL = "instrumented.user@gmail.com";
    private static final String USER_PHONE = "1234567890";
    private static final UUID USER_DEVICE_ID = new UUID(0, 0);

    private static final String IMAGE_DATA = "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";

    ImageViewModel images;

    public ImageViewModelTest() {
        images = new ImageViewModel();
    }

    @BeforeClass
    public static void createTestDependencies() throws InterruptedException {
        TaskHandler.handle(new UserViewModel().createUser(USER_NAME, USER_EMAIL, USER_PHONE, false, USER_DEVICE_ID), userHolder::setUuid);
    }

    @AfterClass
    public static void deleteTestDependencies() throws InterruptedException {
        TaskHandler.handle(new UserViewModel().deleteUser(userHolder.getUuid(), USER_DEVICE_ID, userHolder.getUuid()), _void -> {
        });

        // Deleting account should delete event
    }

    @Test
    public void CreateImageTest() throws InterruptedException {
        TaskHandler.handle(
                images.createImage(userHolder.getUuid(), USER_DEVICE_ID, IMAGE_DATA),
                _ignored -> {
                }
        );

        // Can't delete test images: test user is not an admin
    }
}
