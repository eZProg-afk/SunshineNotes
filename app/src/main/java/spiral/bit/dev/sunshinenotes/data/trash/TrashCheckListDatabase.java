package spiral.bit.dev.sunshinenotes.data.trash;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import spiral.bit.dev.sunshinenotes.models.trash.TrashCheckList;
import spiral.bit.dev.sunshinenotes.models.trash.TrashNote;
import spiral.bit.dev.sunshinenotes.models.trash.TrashTask;

@Database(entities = {TrashCheckList.class, TrashTask.class}, version = 1, exportSchema = false)
public abstract class TrashCheckListDatabase extends RoomDatabase {

    public abstract TrashCheckListDAO getTrashCheckListDAO();

    private static TrashCheckListDatabase trashCheckListDatabase;

    public static synchronized TrashCheckListDatabase getNoteDatabase(Context context) {
        if (trashCheckListDatabase == null) {
            trashCheckListDatabase = Room.databaseBuilder(context,
                    TrashCheckListDatabase.class,
                    "trash_check_lists_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return trashCheckListDatabase;
    }
}
