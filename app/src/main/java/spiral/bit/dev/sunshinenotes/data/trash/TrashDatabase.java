package spiral.bit.dev.sunshinenotes.data.trash;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import spiral.bit.dev.sunshinenotes.models.trash.TrashNote;

@Database(entities = TrashNote.class, version = 1, exportSchema = false)
public abstract class TrashDatabase extends RoomDatabase {

    public abstract TrashDAO getTrashDAO();

    private static TrashDatabase trashNoteDatabase;

    public static synchronized TrashDatabase getNoteDatabase(Context context) {
        if (trashNoteDatabase == null) {
            trashNoteDatabase = Room.databaseBuilder(context,
                    TrashDatabase.class,
                    "trash_notes_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return trashNoteDatabase;
    }
}
