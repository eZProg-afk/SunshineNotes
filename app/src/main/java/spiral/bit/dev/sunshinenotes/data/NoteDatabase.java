package spiral.bit.dev.sunshinenotes.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import spiral.bit.dev.sunshinenotes.models.NoteInFolder;
import spiral.bit.dev.sunshinenotes.models.SimpleNote;

@Database(entities = SimpleNote.class, version = 1, exportSchema = false)
public abstract class NoteDatabase extends RoomDatabase {

    public abstract NoteDAO getNoteDAO();

    private static NoteDatabase noteInFolderDatabase;

    public static synchronized NoteDatabase getNoteDatabase(Context context) {
        if (noteInFolderDatabase == null) {
            noteInFolderDatabase = Room.databaseBuilder(context,
                    NoteDatabase.class,
                    "simple_notes_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return noteInFolderDatabase;
    }
}
