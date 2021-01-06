package spiral.bit.dev.sunshinenotes.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import spiral.bit.dev.sunshinenotes.models.SimpleNote;
import spiral.bit.dev.sunshinenotes.models.other.Statistic;

@Dao
public interface StatisticDAO {

    @Query("SELECT * FROM statistics_table ORDER BY id DESC")
    List<Statistic> getAllStatistic();

    @Query("DELETE FROM statistics_table")
    void deleteStatistic();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertStatistic(Statistic statistic);

    @Delete
    void deleteStatistic(Statistic statistic);
}
