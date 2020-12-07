package spiral.bit.dev.sunshinenotes.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import spiral.bit.dev.sunshinenotes.models.Task;
import spiral.bit.dev.sunshinenotes.models.CheckList;

@Database(entities = {CheckList.class, Task.class}, version = 8, exportSchema = false)
public abstract class CheckListDatabase extends RoomDatabase {

    public abstract CheckListDAO getCheckDAO();

    private static CheckListDatabase checkListDatabase;

    public static synchronized CheckListDatabase getCheckListDatabase(Context context) {
        if (checkListDatabase == null) {
            checkListDatabase = Room.databaseBuilder(context,
                    CheckListDatabase.class,
                    "check_list_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return checkListDatabase;
    }
}
