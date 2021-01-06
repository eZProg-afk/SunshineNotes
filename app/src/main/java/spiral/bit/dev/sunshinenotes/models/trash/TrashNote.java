package spiral.bit.dev.sunshinenotes.models.trash;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "trash_notes")
public class TrashNote implements Serializable {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "note_id")
    private int id;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "date_time")
    private String dateTime;

    @ColumnInfo(name = "date_time_edit")
    private String dateTimeEdit;

    @ColumnInfo(name = "date_time_remind")
    private String dateTimeRemind;

    @ColumnInfo(name = "subtitle")
    private String subTitle;

    @ColumnInfo(name = "font_style")
    private String fontStyle;

    @ColumnInfo(name = "note_color")
    private String noteColor;

    @ColumnInfo(name = "note_text_size")
    private String textSize;

    @ColumnInfo(name = "note_text")
    private String noteText;

    @ColumnInfo(name = "image_path")
    private String imagePath;

    @ColumnInfo(name = "draw_path")
    private String drawPath;

    @ColumnInfo(name = "color")
    private String color;

    @ColumnInfo(name = "web_link")
    private String webLink;

    private boolean isDelete;

    public boolean isDelete() {
        return isDelete;
    }

    public void setDelete(boolean delete) {
        isDelete = delete;
    }

    @NonNull
    @Override
    public String toString() {
        return title + " : " + dateTime;
    }

    public String getNoteColor() {
        return noteColor;
    }

    public void setNoteColor(String noteColor) {
        this.noteColor = noteColor;
    }

    public String getTextSize() {
        return textSize;
    }

    public void setTextSize(String textSize) {
        this.textSize = textSize;
    }

    public String getFontStyle() {
        return fontStyle;
    }

    public void setFontStyle(String fontStyle) {
        this.fontStyle = fontStyle;
    }

    public String getDrawPath() {
        return drawPath;
    }

    public void setDrawPath(String drawPath) {
        this.drawPath = drawPath;
    }

    public String getDateTimeEdit() {
        return dateTimeEdit;
    }

    public void setDateTimeEdit(String dateTimeEdit) {
        this.dateTimeEdit = dateTimeEdit;
    }

    public String getDateTimeRemind() {
        return dateTimeRemind;
    }

    public void setDateTimeRemind(String dateTimeRemind) {
        this.dateTimeRemind = dateTimeRemind;
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

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public String getNoteText() {
        return noteText;
    }

    public void setNoteText(String noteText) {
        this.noteText = noteText;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getWebLink() {
        return webLink;
    }

    public void setWebLink(String webLink) {
        this.webLink = webLink;
    }
}
