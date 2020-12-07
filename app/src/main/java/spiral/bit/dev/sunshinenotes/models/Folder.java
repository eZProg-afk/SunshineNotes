package spiral.bit.dev.sunshinenotes.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "folders_table")
public class Folder implements Serializable {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "folder_id")
    private int id;

    @ColumnInfo(name = "folder_name")
    private String name;

    @ColumnInfo(name = "folder_sub_name")
    private String subTitle;

    @ColumnInfo(name = "folder_date_time")
    private String dateTime;

    @ColumnInfo(name = "folder_color")
    private String color;

    @ColumnInfo(name = "image_path")
    private String imagePath;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
