package spiral.bit.dev.sunshinenotes.data.trash;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import spiral.bit.dev.sunshinenotes.models.trash.TrashCheckList;
import spiral.bit.dev.sunshinenotes.models.trash.TrashNote;
import spiral.bit.dev.sunshinenotes.models.trash.TrashTask;

@Dao
public interface TrashCheckListDAO {

    @Query("SELECT * FROM trash_check_lists ORDER BY check_list_id DESC")
    List<TrashCheckList> getAllTrashCheckLists();

    @Query("SELECT * FROM trash_check_items WHERE parent_id == :parentId ORDER BY parent_id DESC")
    List<TrashTask> getAllTrashTasks(int parentId);

    @Delete
    void deleteTrashCheckList(TrashCheckList trashCheckList);

    @Delete
    void deleteTrashTask(TrashTask trashTask);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTrashCheckList(TrashCheckList trashCheckList);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTrashTask(TrashTask trashTask);

    @Query("DELETE FROM trash_check_lists")
    void autoClearCheckListTrash();

    @Query("DELETE FROM trash_check_items")
    void autoClearTaskTrash();
}
