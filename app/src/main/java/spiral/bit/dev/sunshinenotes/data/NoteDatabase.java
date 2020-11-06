package spiral.bit.dev.sunshinenotes.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import spiral.bit.dev.sunshinenotes.models.Note;

@Database(entities = Note.class, version = 12, exportSchema = false)
public abstract class NoteDatabase extends RoomDatabase {
    public abstract NoteDAO getNoteDAO();

    private static NoteDatabase noteDatabase;

    public static synchronized NoteDatabase getNoteDatabase(Context context) {
        if (noteDatabase == null) {
            noteDatabase = Room.databaseBuilder(context,
                    NoteDatabase.class,
                    "notes_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return noteDatabase;
    }
}
