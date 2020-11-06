package spiral.bit.dev.sunshinenotes.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import spiral.bit.dev.sunshinenotes.models.CheckItem;

@Database(entities = CheckItem.class, version = 2, exportSchema = false)
public abstract class CheckDatabase extends RoomDatabase {
    public abstract CheckDAO getCheckDao();

    private static CheckDatabase checkDatabase;

    public static synchronized CheckDatabase getCheckDatabase(Context context) {
        if (checkDatabase == null) {
            checkDatabase = Room.databaseBuilder(context,
                    CheckDatabase.class,
                    "checks_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return checkDatabase;
    }
}
