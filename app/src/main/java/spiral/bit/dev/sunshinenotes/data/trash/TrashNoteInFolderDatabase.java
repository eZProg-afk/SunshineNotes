package spiral.bit.dev.sunshinenotes.data.trash;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import spiral.bit.dev.sunshinenotes.data.NoteInFolderDAO;
import spiral.bit.dev.sunshinenotes.models.Folder;
import spiral.bit.dev.sunshinenotes.models.NoteInFolder;
import spiral.bit.dev.sunshinenotes.models.trash.TrashFolder;
import spiral.bit.dev.sunshinenotes.models.trash.TrashNoteInFolder;

@Database(entities = {TrashNoteInFolder.class, TrashFolder.class}, version = 1, exportSchema = false)
public abstract class TrashNoteInFolderDatabase extends RoomDatabase {

    public abstract TrashNoteInFolderDAO getNoteDAO();

    private static TrashNoteInFolderDatabase noteInFolderDatabase;

    public static synchronized TrashNoteInFolderDatabase getNoteDatabase(Context context) {
        if (noteInFolderDatabase == null) {
            noteInFolderDatabase = Room.databaseBuilder(context,
                    TrashNoteInFolderDatabase.class,
                    "trash_notes_in_folder_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return noteInFolderDatabase;
    }
}
