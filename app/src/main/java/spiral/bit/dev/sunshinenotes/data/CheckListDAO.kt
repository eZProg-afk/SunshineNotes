package spiral.bit.dev.sunshinenotes.data

import androidx.room.*
import spiral.bit.dev.sunshinenotes.models.CheckList
import spiral.bit.dev.sunshinenotes.models.Task

@Dao
interface CheckListDAO {
    @get:Query("SELECT * FROM check_lists ORDER BY check_list_id DESC")
    val allCheckLists: List<CheckList?>?

    @Query("SELECT * FROM check_items WHERE parent_id = :parentId")
    fun getAllTasks(parentId: Int): List<Task?>?

    @Delete
    fun deleteMultipleCheckLists(checkList: List<CheckList>)

    @Delete
    fun deleteMultipleTasks(tasks: List<Task>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCheckList(checkList: CheckList?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTask(task: Task?)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateTask(task: Task?)

    @Query("DELETE FROM check_lists WHERE check_list_id = :id")
    fun deleteCheckListById(id: Int)

    @Delete
    fun deleteCheckList(checkList: CheckList?)

    @Delete
    fun deleteTask(task: Task?)

    @Query("DELETE FROM check_items WHERE parent_id = :parentId")
    fun deleteCheckById(parentId: Int)

    @Query("DELETE FROM check_items WHERE id = :id")
    fun deleteCheckItemById(id: Int)
}