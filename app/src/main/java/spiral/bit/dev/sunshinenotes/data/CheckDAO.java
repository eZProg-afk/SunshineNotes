package spiral.bit.dev.sunshinenotes.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import spiral.bit.dev.sunshinenotes.models.CheckItem;
import spiral.bit.dev.sunshinenotes.models.Note;

@Dao
public interface CheckDAO {

    @Query("SELECT * FROM checklist_table ORDER BY id DESC")
    List<CheckItem> getAllCheckLists();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCheckList(CheckItem checkItem);

    @Delete
    void deleteCheckList(CheckItem checkItem);
}
