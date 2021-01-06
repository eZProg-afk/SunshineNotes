package spiral.bit.dev.sunshinenotes.models.other;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "statistics_table")
public class Statistic implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String dateText;

    private String typeText;

    private String actionText;

    private String itemSubText;

    private String itemText;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDateText() {
        return dateText;
    }

    public void setDateText(String dateText) {
        this.dateText = dateText;
    }

    public String getTypeText() {
        return typeText;
    }

    public void setTypeText(String typeText) {
        this.typeText = typeText;
    }

    public String getActionText() {
        return actionText;
    }

    public void setActionText(String actionText) {
        this.actionText = actionText;
    }

    public String getItemSubText() {
        return itemSubText;
    }

    public void setItemSubText(String itemSubText) {
        this.itemSubText = itemSubText;
    }

    public String getItemText() {
        return itemText;
    }

    public void setItemText(String itemText) {
        this.itemText = itemText;
    }
}
