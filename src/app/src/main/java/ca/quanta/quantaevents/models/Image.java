package ca.quanta.quantaevents.models;

import java.util.UUID;

/**
 * Class which defines an Image.
 */
public class Image {

    private final UUID imageId;
    private String imageData;

    /**
     * Constructor for an image given a map with some data.
     *
     * @param imageId UUID identifying the image.
     * @param data    Map of image data.
     */
    public Image(UUID imageId, java.util.Map<String, Object> data) {
        this.imageId = imageId;
        this.imageData = data.get("imageData").toString();
    }

    public UUID getImageId() {
        return imageId;
    }

    public String getImageData() {
        return imageData;
    }

}