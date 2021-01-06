package spiral.bit.dev.sunshinenotes.models.other;

public class BackgroundItem {

    private int id;
    private int imageId;

    public BackgroundItem(int id, int imageId) {
        this.id = id;
        this.imageId = imageId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }
}
