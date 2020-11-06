package spiral.bit.dev.sunshinenotes.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "checklist_table")
public class CheckItem implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String checkName;
    private boolean isCompleted;
    private String color;
    @ColumnInfo(name = "date_time")
    private String dateTime;
    @ColumnInfo(name = "date_time_edit")
    private String dateTimeEdit;
    @ColumnInfo(name = "image_path")
    private String imagePath;
    @ColumnInfo(name = "date_time_remind")
    private String dateTimeRemind;

    public String getDateTimeRemind() {
        return dateTimeRemind;
    }

    public void setDateTimeRemind(String dateTimeRemind) {
        this.dateTimeRemind = dateTimeRemind;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getDateTimeEdit() {
        return dateTimeEdit;
    }

    public void setDateTimeEdit(String dateTimeEdit) {
        this.dateTimeEdit = dateTimeEdit;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCheckName() {
        return checkName;
    }

    public void setCheckName(String checkName) {
        this.checkName = checkName;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
}
