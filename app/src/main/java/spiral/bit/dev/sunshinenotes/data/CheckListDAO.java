package spiral.bit.dev.sunshinenotes.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import spiral.bit.dev.sunshinenotes.models.Task;
import spiral.bit.dev.sunshinenotes.models.CheckList;

@Dao
public interface CheckListDAO {

    @Query("SELECT * FROM check_lists ORDER BY check_list_id DESC")
    List<CheckList> getAllCheckLists();

    @Query("SELECT * FROM check_items WHERE parent_id = :parentId")
    List<Task> getAllTasks(int parentId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCheckList(CheckList checkList);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTask(Task task);

    @Query("DELETE FROM check_lists WHERE check_list_id = :id")
    void deleteCheckListById(int id);

        @Delete
    void deleteCheckList(CheckList checkList);

    @Delete
    void deleteTask(Task task);

    @Query("DELETE FROM check_items WHERE parent_id = :parentId")
    void deleteCheckById(int parentId);
}
