package ca.quanta.quantaevents.models;

import java.util.UUID;

/**
 * Class which defines an Image.
 */
public class Image {

    private final UUID imageId;
    private String imageData;

    /**
     * Constructor for an Image object.
     * @param imageData Base64 encoded string of image data.
     */
    Image(String imageData) {
        imageId = UUID.randomUUID();
        this.imageData = imageData;
    }

}