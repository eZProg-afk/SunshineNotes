package spiral.bit.dev.sunshinenotes.data.trash;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import spiral.bit.dev.sunshinenotes.models.trash.TrashNote;

@Dao
public interface TrashDAO {

    @Query("SELECT * FROM trash_notes ORDER BY note_id DESC")
    List<TrashNote> getAllTrashNotes();

    @Delete
    void deleteTrashNote(TrashNote trashNote);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTrashNote(TrashNote trashNote);

    @Query("DELETE FROM trash_notes")
    void autoClearTrash();
}
