package spiral.bit.dev.sunshinenotes.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import spiral.bit.dev.sunshinenotes.models.other.Statistic;

@Database(entities = Statistic.class, version = 1, exportSchema = false)
public abstract class StatisticDatabase extends RoomDatabase {

    public abstract StatisticDAO getStatisticDAO();

    private static StatisticDatabase statisticDatabase;

    public static synchronized StatisticDatabase getStatisticDatabase(Context context) {
        if (statisticDatabase == null) {
            statisticDatabase = Room.databaseBuilder(context,
                    StatisticDatabase.class,
                    "statistic_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return statisticDatabase;
    }
}
