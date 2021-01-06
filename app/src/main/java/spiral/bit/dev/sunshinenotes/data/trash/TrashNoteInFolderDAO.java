package spiral.bit.dev.sunshinenotes.data.trash;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import spiral.bit.dev.sunshinenotes.models.Folder;
import spiral.bit.dev.sunshinenotes.models.NoteInFolder;
import spiral.bit.dev.sunshinenotes.models.trash.TrashFolder;
import spiral.bit.dev.sunshinenotes.models.trash.TrashNoteInFolder;

@Dao
public interface TrashNoteInFolderDAO {

    @Query("SELECT * FROM trash_notes_in_folder_table ORDER BY note_id DESC")
    List<TrashNoteInFolder> getAllNotes();

    @Delete
    void deleteMultiplyNotes(List<TrashNoteInFolder> folders);

    @Query("SELECT * FROM trash_folders_table ORDER BY folder_id DESC")
    List<TrashFolder> getAllFolders();

    @Query("SELECT * FROM trash_notes_in_folder_table WHERE child_id = :parentID")
    List<TrashNoteInFolder> getAllNotesInFolder(int parentID);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertFolder(TrashFolder trashFolder);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertNote(TrashNoteInFolder trashNoteInFolder);

    @Query("DELETE FROM trash_notes_in_folder_table WHERE note_id = :parentId")
    void deleteNoteById(int parentId);

    @Delete
    void deleteFolder(TrashFolder trashFolder);

    @Delete
    void deleteMultipleFolders(List<TrashFolder> trashFolders);

    @Delete
    void deleteNote(TrashNoteInFolder trashNoteInFolder);

    @Query("DELETE FROM trash_folders_table")
    void autoClearFoldersTrash();

    @Query("DELETE FROM trash_notes_in_folder_table")
    void autoClearNotesTrash();
}
