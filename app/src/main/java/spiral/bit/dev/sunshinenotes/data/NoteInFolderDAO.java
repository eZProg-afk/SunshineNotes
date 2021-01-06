package spiral.bit.dev.sunshinenotes.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import spiral.bit.dev.sunshinenotes.models.Folder;
import spiral.bit.dev.sunshinenotes.models.NoteInFolder;

@Dao
public interface NoteInFolderDAO {

    @Query("SELECT * FROM notes_table ORDER BY note_id DESC")
    List<NoteInFolder> getAllNotes();

    @Delete
    void deleteMultiplyNotes(List<NoteInFolder> folders);

    @Query("SELECT * FROM folders_table ORDER BY folder_id DESC")
    List<Folder> getAllFolders();

    @Query("SELECT * FROM notes_table WHERE child_id = :parentID")
    List<NoteInFolder> getAllNotesInFolder(int parentID);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertFolder(Folder folder);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertNote(NoteInFolder noteInFolder);

    @Query("DELETE FROM notes_table WHERE note_id = :parentId")
    void deleteNoteById(int parentId);

    @Delete
    void deleteFolder(Folder folder);

    @Delete
    void deleteMultipleFolders(List<Folder> folders);

    @Delete
    void deleteNote(NoteInFolder noteInFolder);
}
