package spiral.bit.dev.sunshinenotes.models.trash;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "trash_check_lists")
public class TrashCheckList implements Serializable {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "check_list_id")
    private int checkListId;

    @ColumnInfo(name = "check_list_title")
    private String title;

    @ColumnInfo(name = "check_list_date_time")
    private String dateTime;

    @ColumnInfo(name = "check_list_date_time_edit")
    private String dateTimeEdit;

    @ColumnInfo(name = "check_list_image")
    private String imagePath;

    @ColumnInfo(name = "check_list_color")
    private String checkListColor;

    @ColumnInfo(name = "is_completed")
    private boolean isCompleted;

    public int getCheckListId() {
        return checkListId;
    }

    public void setCheckListId(int checkListId) {
        this.checkListId = checkListId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public String getCheckListColor() {
        return checkListColor;
    }

    public void setCheckListColor(String checkListColor) {
        this.checkListColor = checkListColor;
    }

    public String getImagePath() {
        return imagePath;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
