package spiral.bit.dev.sunshinenotes.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import spiral.bit.dev.sunshinenotes.models.Folder;
import spiral.bit.dev.sunshinenotes.models.NoteInFolder;
import spiral.bit.dev.sunshinenotes.models.SimpleNote;

@Dao
public interface NoteDAO {

    @Query("SELECT * FROM simple_notes_table ORDER BY note_id DESC")
    List<SimpleNote> getAllNotes();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertNote(SimpleNote simpleNote);

    @Delete
    void deleteNote(SimpleNote simpleNote);
}
