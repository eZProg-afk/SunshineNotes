package spiral.bit.dev.sunshinenotes.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import spiral.bit.dev.sunshinenotes.models.Folder;
import spiral.bit.dev.sunshinenotes.models.NoteInFolder;

@Database(entities = {NoteInFolder.class, Folder.class}, version = 14, exportSchema = false)
public abstract class NoteInFolderDatabase extends RoomDatabase {

    public abstract NoteInFolderDAO getNoteDAO();

    private static NoteInFolderDatabase noteInFolderDatabase;

    public static synchronized NoteInFolderDatabase getNoteDatabase(Context context) {
        if (noteInFolderDatabase == null) {
            noteInFolderDatabase = Room.databaseBuilder(context,
                    NoteInFolderDatabase.class,
                    "notes_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return noteInFolderDatabase;
    }
}
