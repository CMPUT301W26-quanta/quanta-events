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
    public Image(String imageData) {
        imageId = UUID.randomUUID();
        this.imageData = imageData;
    }

    /**
     * Constructor for a Image object when user and device ID are known.
     * @param imageId Image UUID.
     * @param imageData Base64 encoded string data of image data.
     */
    public Image(UUID imageId, String imageData) {
        this.imageId = imageId;
        this.imageData = imageData;
    }

    public Image(UUID imageId, java.util.Map<String, Object> data) {
        this.imageId = imageId;
        this.imageData = data.get("imageData").toString();
    }

    public String getImageData() {
        return imageData;
    }

}