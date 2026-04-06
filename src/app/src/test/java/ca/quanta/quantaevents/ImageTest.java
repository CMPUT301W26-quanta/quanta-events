package ca.quanta.quantaevents;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import ca.quanta.quantaevents.models.Image;

public class ImageTest {
    private static final UUID IMAGE_ID = UUID.randomUUID();
    private static final String IMAGE_DATA = "IMAGE DATA GOES HERE";

    @Test
    public void ImageCreationTest() {
        Map<String, Object> data = new HashMap<>();
        data.put("imageData", IMAGE_DATA);
        Image image = new Image(IMAGE_ID, data);

        assertEquals(IMAGE_ID, image.getImageId());
        assertEquals(IMAGE_DATA, image.getImageData());
    }
}
