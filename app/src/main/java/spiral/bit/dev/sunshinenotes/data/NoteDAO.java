package spiral.bit.dev.sunshinenotes.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;
import spiral.bit.dev.sunshinenotes.models.Note;

@Dao
public interface NoteDAO {

    @Query("SELECT * FROM notes_table ORDER BY note_id DESC")
    List<Note> getAllNotes();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertNote(Note note);

    @Delete
    void deleteNote(Note note);

    @Delete
    void deleteMultiplyNotes(List<Note> notes);
}
