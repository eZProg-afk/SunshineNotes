package spiral.bit.dev.sunshinenotes.models.trash;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.io.Serializable;

import spiral.bit.dev.sunshinenotes.models.CheckList;

@Entity(tableName = "trash_check_items",
        foreignKeys = @ForeignKey(entity = TrashCheckList.class, parentColumns = "check_list_id",
                childColumns = "parent_id"))
public class TrashTask implements Serializable {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;

    @ColumnInfo(name = "parent_id")
    private int parentId;

    @ColumnInfo(name = "check_list_title")
    private String title;

    @ColumnInfo(name = "check_list_date_time")
    private String dateTime;

    @ColumnInfo(name = "is_completed")
    private boolean isCompleted;

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

}
